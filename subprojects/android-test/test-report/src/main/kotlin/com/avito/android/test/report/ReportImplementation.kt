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
import io.sentry.SentryClient
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
    // TODO hide sentry
    override val sentry: SentryClient,
    private val onIncident: (Throwable) -> Unit = {},
    private val performanceTestReporter: PerformanceTestReporter,
    private val logger: Logger,
    private val transport: List<Transport>
) : Report,
    StepLifecycleListener by StepLifecycleNotifier,
    TestLifecycleListener by TestLifecycleNotifier,
    PreconditionLifecycleListener by PreconditionLifecycleNotifier,
    ReportStateProvider {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private val remoteStorage: RemoteStorage = RemoteStorage.create(
        logger = logger,
        httpClient = httpClient,
        endpoint = fileStorageUrl
    )

    private val screenshotUploader: ScreenshotUploader = ScreenshotUploader.Impl(
        screenshotCapturer = ScreenshotCapturer.Impl(onDeviceCacheDirectory, logger),
        remoteStorage = remoteStorage,
        logger = logger
    )

    /**
     * Entries that occurred before first step/precondition
     */
    private val earlyEntries = mutableListOf<Entry>()
    private val earlyFuturesUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private val incidentEntries = mutableListOf<Entry>()
    private val incidentFutureUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private var state: ReportState = ReportState.Nothing

    override val isFirstStepOrPrecondition: Boolean
        get() {
            val currentState = state

            return currentState is ReportState.Nothing || currentState is ReportState.Initialized ||
                (currentState is ReportState.Initialized.Started && (currentState.preconditionNumber == 1 ||
                    // на случай если нет precondition, а сразу начинаем со step
                    (currentState.preconditionNumber == 0 && currentState.stepNumber == 1)))
        }

    // Используется только для тестов
    @Synchronized
    override fun getCurrentState(): ReportState = state

    @Synchronized
    override fun initTestCase(testMetadata: TestMetadata) = methodExecutionTracing("initTestCase") {
        val currentState = state

        if (currentState !is ReportState.Nothing) {
            throw RuntimeException("Reporter has invalid state. Expected state: Nothing. Actual state: $state")
        }

        state = ReportState.Initialized.WaitingToStart(testMetadata)
    }

    @Synchronized
    override fun startTestCase(): Unit = methodExecutionTracing("startTestCase") {
        val currentState = state
        if (currentState !is ReportState.Initialized.WaitingToStart) {
            throw RuntimeException("Reporter has invalid state. Expected state: Initialized.WaitingToStart. Actual state: $state")
        }

        state = ReportState.Initialized.Started(
            testMetadata = currentState.testMetadata,
            currentStep = null,
            incident = null,
            stepNumber = 0,
            preconditionNumber = 0,
            startTime = timeProvider.nowInSeconds()
        )

        beforeTestStart(state as ReportState.Initialized.Started)
    }

    @Synchronized
    override fun updateTestCase(update: ReportState.Initialized.Started.() -> Unit): Unit =
        methodExecutionTracing("updateTestCase") {
            val currentState = state
            if (currentState !is ReportState.Initialized.Started) {
                throw RuntimeException("Reporter has invalid state. Expected state: Initialized. Actual state: $state")
            }

            currentState.apply {
                beforeTestUpdate(this)
                update(currentState)
                afterTestUpdate(this)
            }
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
        val currentState = state
        if (currentState !is ReportState.Initialized) {
            throw RuntimeException("Reporter has invalid state. Expected state: Initialized. Actual state: $state")
        }

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
            val startedState = state
            if (startedState !is ReportState.Initialized.Started) {
                throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
            }
            startedState.endTime = timeProvider.nowInSeconds()

            try {
                afterTestStop(startedState)
            } catch (t: Throwable) {
                logger.exception("Failed while afterTestStop were executing", t)
            }

            waitUploads(startedState)

            startedState.addEarlyEntries(earlyEntries)
            startedState.sortStepEntries()

            startedState.incident?.appendFutureEntries()
            startedState.performanceJson = performanceTestReporter.getAsJson()

            writeTestCase(currentState = startedState)

            startedState
        }

    /**
     * add early entries to first precondition/step
     */
    private fun ReportState.Initialized.Started.addEarlyEntries(entries: List<Entry>) {
        val firstPreconditionOrStep =
            preconditionStepList.firstOrNull() ?: testCaseStepList.firstOrNull()

        firstPreconditionOrStep?.entryList?.addAll(earlyEntries)
    }

    private fun ReportState.Initialized.Started.sortStepEntries() {
        preconditionStepList.forEach {
            it.entryList = it.entryList
                .sortedBy { it.timeInSeconds }
                .distinctCounted()
                .toMutableList()
        }
        testCaseStepList.forEach {
            it.entryList = it.entryList
                .sortedBy { it.timeInSeconds }
                .distinctCounted()
                .toMutableList()
        }
    }

    @Synchronized
    override fun startPrecondition(step: StepResult): Unit =
        methodExecutionTracing("startPrecondition") {
            val currentState = state
            if (currentState !is ReportState.Initialized.Started) {
                throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
            }

            currentState.currentStep = step

            step.apply {
                timestamp = timeProvider.nowInSeconds()
                number = currentState.preconditionNumber++
                beforePreconditionStart(this)
            }
        }

    @Synchronized
    override fun stopPrecondition(): Unit = methodExecutionTracing("stopPrecondition") {
        val currentState = state
        if (currentState !is ReportState.Initialized.Started) {
            throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
        }

        val currentStep = currentState.currentStep
        if (currentStep == null) {
            throw RuntimeException("Couldn't stop precondition because it hasn't started yet")
        }

        currentStep.apply {
            currentState.preconditionStepList.add(this)
            afterPreconditionStop(this)
        }
    }

    @Synchronized
    override fun startStep(step: StepResult): Unit = methodExecutionTracing("startStep") {
        val currentState = state
        if (currentState !is ReportState.Initialized.Started) {
            throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
        }

        currentState.currentStep = step

        step.apply {
            timestamp = timeProvider.nowInSeconds()
            number = currentState.stepNumber++
            beforeStepStart(this)
        }
    }

    @Synchronized
    override fun updateStep(update: StepResult.() -> Unit): Unit =
        methodExecutionTracing("updateStep") {
            val currentState = state
            if (currentState !is ReportState.Initialized.Started) {
                throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
            }

            val currentStep = currentState.currentStep
            if (currentStep == null) {
                throw RuntimeException("Couldn't stop precondition because it hasn't started yet")
            }

            currentStep.apply {
                beforeStepUpdate(this)
                update()
                afterStepUpdate(this)
            }
        }

    @Synchronized
    override fun stopStep(): Unit = methodExecutionTracing("stopStep") {
        val currentState = state
        if (currentState !is ReportState.Initialized.Started) {
            throw RuntimeException("Reporter has invalid state. Expected state: Started. Actual state: $state")
        }

        val currentStep = currentState.currentStep
        if (currentStep == null) {
            throw RuntimeException("Couldn't stop precondition because it hasn't started yet")
        }

        currentStep.apply {
            currentState.testCaseStepList.add(this)
            afterStepStop(this)
        }
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
        remoteStorage.upload(
            uploadRequest = RemoteStorage.Request.ContentRequest(
                content = wrapInHtml(content),
                extension = Entry.File.Type.html.name
            ),
            comment = label
        ).let {
            val currentState = state

            if (currentState is ReportState.Initialized.Started && currentState.currentStep != null) {
                currentState.currentStep!!.futureUploads.add(it)
            } else {
                earlyFuturesUploads.add(it)
            }
        }
    }

    @Synchronized
    override fun addEntry(entry: Entry) {
        val currentState = state

        if (currentState is ReportState.Initialized.Started && currentState.currentStep != null) {
            currentState.currentStep!!.entryList.add(entry)
        } else {
            earlyEntries.add(entry)
        }
    }

    @Synchronized
    override fun addComment(comment: String) {
        addEntry(Entry.Comment(comment, timeProvider.nowInSeconds()))
    }

    @Synchronized
    override fun addAssertion(assertionMessage: String) {
        addEntry(Entry.Check(assertionMessage, timeProvider.nowInSeconds()))
    }

    private data class Counted<T>(val t: T, var count: Int = 1)

    /**
     * сворачивает встречающиеся подряд одинаковые entry в один, проставляя им в начало лейбла кол-во свернутых элементов
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Entry> List<T>.distinctCounted(): List<T> =
        fold(listOf<Counted<T>>()) { acc, entry ->
            val last = acc.lastOrNull()
            val lastT = last?.t
            if (entry is Entry.Comment && lastT is Entry.Comment && entry.title == lastT.title) {
                acc.apply { this.last().count++ }
            } else {
                acc + Counted(entry, 1)
            }
        }.map { counted: Counted<T> ->
            if (counted.t is Entry.Comment && counted.count > 1) {
                counted.t.copy(title = "[x${counted.count}] ${counted.t.title}")
            } else {
                counted.t
            }
        } as List<T>

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

    private fun writeTestCase(currentState: ReportState.Initialized) =
        methodExecutionTracing("writeTestCase") {
            if (currentState !is ReportState.Initialized.Started) {
                throw RuntimeException("Reporter has invalid state. Expected state: Started.Initialized. Actual state: $state")
            }

            beforeTestWrite(currentState)
            transport.forEach { it.send(currentState) }
            state = ReportState.Written
        }

    /**
     * Screenshots/HttpStatic are synchronous, but uploading runs on background thread
     * We have to wait upload completion before sending report packages
     */
    private fun waitUploads(state: ReportState.Initialized.Started) {
        state.testCaseStepList =
            state.testCaseStepList
                .map { it.appendFutureEntries() }
                .toMutableList()

        state.preconditionStepList =
            state.preconditionStepList
                .map { it.appendFutureEntries() }
                .toMutableList()

        earlyEntries.addAll(
            earlyFuturesUploads.getInitializedEntries()
        )
        earlyEntries.sortBy { it.timeInSeconds }

        incidentEntries.addAll(
            incidentFutureUploads.getInitializedEntries()
        )
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
