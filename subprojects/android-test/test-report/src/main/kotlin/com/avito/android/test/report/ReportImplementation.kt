package com.avito.android.test.report

import android.annotation.SuppressLint
import com.avito.android.test.report.incident.AppCrashIncidentPresenter
import com.avito.android.test.report.incident.FallbackIncidentPresenter
import com.avito.android.test.report.incident.IncidentChain
import com.avito.android.test.report.incident.RequestIncidentPresenter
import com.avito.android.test.report.incident.ResourceIncidentPresenter
import com.avito.android.test.report.incident.ResourceManagerIncidentPresenter
import com.avito.android.test.report.incident.TestCaseIncidentPresenter
import com.avito.android.test.report.listener.PreconditionLifecycleListener
import com.avito.android.test.report.listener.PreconditionLifecycleNotifier
import com.avito.android.test.report.listener.StepLifecycleListener
import com.avito.android.test.report.listener.StepLifecycleNotifier
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.screenshot.ScreenshotUploader
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.Logger
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import java.io.File

/**
 * Assume no parallelization
 * Assume single test per object
 *
 * @param onDeviceCacheDirectory для всяких скриншотов, видео, логов
 */
@Suppress("FoldInitializerAndIfToElvis")
@SuppressLint("LogNotTimber")
class ReportImplementation(
    onDeviceCacheDirectory: Lazy<File>,
    httpClient: OkHttpClient,
    fileStorageUrl: String,
    private val onIncident: (Throwable) -> Unit = {},
    private val performanceTestReporter: PerformanceTestReporter,
    private val logger: Logger,
    private val transport: List<Transport>,
    private val remoteStorage: RemoteStorage = RemoteStorage.create(
        logger = logger,
        httpClient = httpClient,
        endpoint = fileStorageUrl
    ),
    private val screenshotUploader: ScreenshotUploader = ScreenshotUploader.Impl(
        screenshotCapturer = ScreenshotCapturer.Impl(onDeviceCacheDirectory, logger),
        remoteStorage = remoteStorage,
        logger = logger
    ),
    private val timeProvider: TimeProvider = DefaultTimeProvider()
) : Report,
    StepLifecycleListener by StepLifecycleNotifier,
    TestLifecycleListener by TestLifecycleNotifier,
    PreconditionLifecycleListener by PreconditionLifecycleNotifier {


    /**
     * Entries that occurred before first step/precondition
     */
    private val earlyEntries = mutableListOf<Entry>()
    private val earlyFuturesUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private val incidentFutureUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private var state: ReportState = ReportState.Nothing

    override val isFirstStepOrPrecondition: Boolean
        get() = state.isFirstStepOrPrecondition


    @Synchronized
    override fun initTestCase(testMetadata: TestMetadata) = methodExecutionTracing("initTestCase") {
        checkStateIs<ReportState.Nothing>()
        state = ReportState.Initialized.NotStarted(testMetadata)
    }

    @Synchronized
    override fun startTestCase(): Unit = methodExecutionTracing("startTestCase") {
        val currentState = getCastedState<ReportState.Initialized.NotStarted>()

        val started = ReportState.Initialized.Started(
            testMetadata = currentState.testMetadata,
            currentStep = null,
            incident = null,
            stepNumber = 0,
            preconditionNumber = 0,
            startTime = timeProvider.nowInSeconds()
        )
        state = started
        beforeTestStart(started)
    }

    @Synchronized
    override fun updateTestCase(update: ReportState.Initialized.Started.() -> Unit): Unit =
        methodExecutionTracing("updateTestCase") {
            val currentState = getCastedState<ReportState.Initialized.Started>()
            beforeTestUpdate(currentState)
            update(currentState)
            afterTestUpdate(currentState)
        }

    @Synchronized
    override fun registerIncident(exception: Throwable) {
        registerIncident(
            exception = exception,
            screenshot = null,
            type = exception.determineIncidentType()
        )
    }

    @Synchronized
    override fun registerIncident(
        exception: Throwable,
        screenshot: FutureValue<RemoteStorage.Result>?,
        type: Incident.Type
    ) = methodExecutionTracing("registerIncident") {
        val currentState = getCastedState<ReportState.Initialized>()

        if (currentState.incident == null) {
            val incidentChain = IncidentChain.Impl(
                customViewPresenters = setOf(
                    TestCaseIncidentPresenter(),
                    RequestIncidentPresenter(),
                    ResourceIncidentPresenter(),
                    ResourceManagerIncidentPresenter(),
                    AppCrashIncidentPresenter()
                ),
                fallbackPresenter = FallbackIncidentPresenter()
            ).toChain(exception)

            val incidentToAdd = Incident(
                type = type,
                chain = incidentChain,
                timestamp = timeProvider.nowInSeconds(),
                entryList = emptyList(),
                // don't need trace right now(mb in some debug mode in RV? MBS-2148)
                // trace = exception?.formatStackTrace() ?: listOf("StackTrace недоступен")
                trace = emptyList()
            )

            if (screenshot != null) {
                incidentFutureUploads.add(screenshot)
            }

            onIncident.invoke(exception)

            currentState.incident = incidentToAdd
            afterIncident(incidentToAdd)
        }
    }

    @Synchronized
    override fun reportTestCase(): ReportState.Initialized.Started =
        methodExecutionTracing("reportTestCase") {
            val startedState = getCastedState<ReportState.Initialized.Started>()
            startedState.endTime = timeProvider.nowInSeconds()

            try {
                afterTestStop(startedState)
            } catch (t: Throwable) {
                logger.exception("Failed while afterTestStop were executing", t)
            }
            earlyEntries.addAll(
                earlyFuturesUploads.getInitializedEntries()
            )
            startedState.waitUploads()
            startedState.addEarlyEntries(earlyEntries)
            startedState.sortStepEntries()
            startedState.incident?.appendFutureEntries()
            startedState.performanceJson = performanceTestReporter.getAsJson()
            startedState.writeTestCase()
            startedState
        }

    @Synchronized
    override fun startPrecondition(step: StepResult): Unit =
        methodExecutionTracing("startPrecondition") {
            val currentState = getCastedState<ReportState.Initialized.Started>()

            currentState.currentStep = step

            step.timestamp = timeProvider.nowInSeconds()
            step.number = currentState.preconditionNumber++
            beforePreconditionStart(step)
        }

    @Synchronized
    override fun stopPrecondition(): Unit = methodExecutionTracing("stopPrecondition") {
        val currentState = getCastedState<ReportState.Initialized.Started>()

        val currentStep = currentState.currentStep
        if (currentStep == null) {
            throw RuntimeException("Couldn't stop precondition because it hasn't started yet")
        }

        currentState.preconditionStepList.add(currentStep)
        afterPreconditionStop(currentStep)
        currentState.currentStep = null
    }

    @Synchronized
    override fun startStep(step: StepResult): Unit = methodExecutionTracing("startStep") {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        currentState.currentStep = step
        step.timestamp = timeProvider.nowInSeconds()
        step.number = currentState.stepNumber++
        beforeStepStart(step)
    }

    @Synchronized
    override fun stopStep(): Unit = methodExecutionTracing("stopStep") {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Couldn't stop step because it hasn't started yet"
        }

        currentState.testCaseStepList.add(currentStep)
        afterStepStop(currentStep)
        currentState.currentStep = null
    }

    private fun updateStep(update: StepResult.() -> Unit): Unit =
        methodExecutionTracing("updateStep") {
            val currentState = getCastedState<ReportState.Initialized.Started>()
            val currentStep = requireNotNull(currentState.currentStep) {
                "Couldn't upate step because it hasn't started yet"
            }

            beforeStepUpdate(currentStep)
            currentStep.update()
            afterStepUpdate(currentStep)
        }

    @Synchronized
    override fun makeScreenshot(comment: String): FutureValue<RemoteStorage.Result>? =
        methodExecutionTracing("stopStep") {
            val screenshotFuture = screenshotUploader.makeAndUploadScreenshot(comment)

            if (screenshotFuture != null) {
                try {
                    updateStep {
                        futureUploads.add(screenshotFuture)
                    }
                } catch (t: Throwable) {
                    logger.critical("Failed to update step with captured screenshot", t)
                    return@methodExecutionTracing null
                }
            }

            screenshotFuture
        }

    @Synchronized
    override fun addHtml(label: String, content: String) {
        val html = remoteStorage.upload(
            uploadRequest = RemoteStorage.Request.ContentRequest(
                content = wrapInHtml(content),
                extension = Entry.File.Type.html.name
            ),
            comment = label
        )
        val started = getCastedStateOrNull<ReportState.Initialized.Started>()
        val futureUploads = started?.getCurrentStepOrCreate {
            StepResult(
                timestamp = timeProvider.nowInSeconds(),
                number = started.stepNumber++,
                title = "Synthetic step"
            )
        }?.futureUploads ?: earlyFuturesUploads
        futureUploads.add(html)
    }


    @Synchronized
    override fun addComment(comment: String) {
        addEntry(Entry.Comment(comment, timeProvider.nowInSeconds()))
    }

    @Synchronized
    override fun addAssertion(assertionMessage: String) {
        addEntry(Entry.Check(assertionMessage, timeProvider.nowInSeconds()))
    }

    private fun addEntry(entry: Entry) {
        val started = getCastedStateOrNull<ReportState.Initialized.Started>()
        val entriesList = started?.getCurrentStepOrCreate {
            StepResult(
                timestamp = timeProvider.nowInSeconds(),
                number = started.stepNumber++,
                title = "Synthetic step"
            )
        }?.entryList ?: earlyEntries
        entriesList.add(entry)
    }

    private inline fun <reified T : ReportState> checkStateIs() = getCastedState<T>()

    private inline fun <reified T : ReportState> getCastedStateOrNull(): T? {
        return when (val _state = state) {
            is T -> _state
            else -> null
        }
    }

    private inline fun <reified T : ReportState> getCastedState(): T {
        return getCastedStateOrNull()
            ?: throw IllegalStateException("Invalid state. Expected ${T::class.java} actual $state")
    }

    private fun StepResult.appendFutureEntries(): StepResult {
        if (futureUploads.isEmpty()) return this
        return copy(entryList = (entryList + futureUploads.getInitializedEntries()).toMutableList())
    }

    private fun Incident.appendFutureEntries(): Incident {
        if (incidentFutureUploads.isEmpty()) return this
        return copy(entryList = entryList + incidentFutureUploads.getInitializedEntries())
    }

    private fun List<FutureValue<RemoteStorage.Result>>.getInitializedEntries(): List<Entry> =
        asSequence()
            .map(FutureValue<RemoteStorage.Result>::get)
            .filterIsInstance<RemoteStorage.Result.Success>()
            .map {
                Entry.File(
                    comment = it.comment,
                    fileAddress = it.url,
                    timeInSeconds = it.timeInSeconds,
                    fileType = when (it.uploadRequest) {
                        is RemoteStorage.Request.FileRequest.Image -> Entry.File.Type.img_png
                        is RemoteStorage.Request.FileRequest.Video -> Entry.File.Type.video
                        is RemoteStorage.Request.ContentRequest -> Entry.File.Type.html
                    }
                )
            }
            .toList()

    private fun ReportState.Initialized.Started.writeTestCase() =
        methodExecutionTracing("writeTestCase") {
            beforeTestWrite(this)
            transport.forEach { it.send(this) }
            state = ReportState.Written
        }

    /**
     * Screenshots/HttpStatic are synchronous, but uploading runs on background thread
     * We have to wait upload completion before sending report packages
     */
    private fun ReportState.Initialized.Started.waitUploads() {
        testCaseStepList =
            testCaseStepList
                .map { it.appendFutureEntries() }
                .toMutableList()

        preconditionStepList =
            preconditionStepList
                .map { it.appendFutureEntries() }
                .toMutableList()
    }

    private fun wrapInHtml(content: String): String {
        return """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
</head>
<body>
<pre>
$content
</pre>
</body>
</html>""".trimIndent()
    }

    private fun <T> methodExecutionTracing(name: String, action: () -> T): T {
        logger.debug("Method: $name execution started")
        val result = action()
        logger.debug("Method: $name execution completed")
        return result
    }
}
