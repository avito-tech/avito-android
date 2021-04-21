package com.avito.android.test.report

import androidx.annotation.VisibleForTesting
import androidx.test.espresso.EspressoException
import com.avito.android.Result
import com.avito.android.test.report.incident.AppCrashException
import com.avito.android.test.report.incident.AppCrashIncidentPresenter
import com.avito.android.test.report.incident.FallbackIncidentPresenter
import com.avito.android.test.report.incident.IncidentChainFactory
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
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.model.Incident
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

/**
 * Assume no parallelization
 * Assume single test per object
 *
 * @param onDeviceCacheDirectory for screenshots/videos/logs
 */
class ReportImplementation(
    loggerFactory: LoggerFactory,
    private val transport: Transport,
    private val screenshotCapturer: ScreenshotCapturer,
    private val timeProvider: TimeProvider,
    private val troubleshooter: Troubleshooter
) : InternalReport,
    ReportStepModelFactory<StepResult>,
    StepLifecycleListener by StepLifecycleNotifier,
    TestLifecycleListener by TestLifecycleNotifier,
    PreconditionLifecycleListener by PreconditionLifecycleNotifier {

    private val logger = loggerFactory.create<InternalReport>()

    /**
     * Entries that occurred before first step/precondition
     */
    private val earlyEntries = mutableListOf<Entry>()
    private val earlyFuturesUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private val incidentFutureUploads = mutableListOf<FutureValue<RemoteStorage.Result>>()

    private var state: ReportState = ReportState.Nothing

    @VisibleForTesting
    val currentState: ReportState
        get() = state

    override val isFirstStep: Boolean
        get() = state.isFirstStepOrPrecondition

    private val isWritten: Boolean
        get() = currentState is ReportState.Written

    @Synchronized
    override fun createStepModel(stepName: String): StepResult {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = currentState.stepNumber++
        )
    }

    @Synchronized
    override fun createPreconditionModel(stepName: String): StepResult {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = currentState.preconditionNumber++
        )
    }

    @Synchronized
    override fun unexpectedFailedTestCase(exception: Throwable) {
        if (!isWritten) {
            registerIncident(AppCrashException(exception), null)
            finishTestCase()
        } else {
            logger.warn("Fail to register unexpected incident. Report is already written", exception)
        }
    }

    private fun registerIncident(
        exception: Throwable,
        screenshotName: String
    ) {
        registerIncident(exception, makeScreenshot(screenshotName).getOrElse { null })
    }

    private fun registerIncident(
        exception: Throwable,
        screenshot: FutureValue<RemoteStorage.Result>?
    ) = methodExecutionTracing("registerIncident") {
        val currentState = getCastedState<ReportState.Initialized>(cause = exception)

        if (currentState.incident == null) {
            if (screenshot != null) {
                incidentFutureUploads.add(screenshot)
            }

            val type = exception.determineIncidentType()
            val chainFactory = createIncidentChainFactory()
            val incidentToAdd = Incident(
                type = type,
                chain = chainFactory.toChain(exception),
                timestamp = timeProvider.nowInSeconds(),
                entryList = emptyList(),
                trace = exception.stackTraceToList()
            )
            addTroubleshootingEntries()
            currentState.incident = incidentToAdd
            afterIncident(incidentToAdd)
        } else {
            logger.warn("Fail to register incident. Incident already exist", exception)
        }
    }

    private fun addTroubleshootingEntries() {
        troubleshooter.troubleshootTo(this)
    }

    @Synchronized
    override fun finishTestCase() {
        methodExecutionTracing("finishTestCase") {
            val startedState = getCastedState<ReportState.Initialized.Started>()
            startedState.endTime = timeProvider.nowInSeconds()

            try {
                afterTestStop(startedState)
            } catch (t: Throwable) {
                logger.warn("Failed while afterTestStop were executing", t)
            }
            earlyEntries.addAll(
                earlyFuturesUploads.getInitializedEntries()
            )

            with(startedState) {
                waitUploads()
                addEarlyEntries(earlyEntries)
                sortStepEntries()
                incident?.appendFutureEntries()
                writeTestCase()
            }
        }
    }

    @Synchronized
    override fun failedTestCase(exception: Throwable) {
        registerIncident(exception, "Failed test case")
    }

    @Synchronized
    override fun setDataSet(value: DataSet) {
        methodExecutionTracing("setDataSet") {
            val currentState = getCastedState<ReportState.Initialized.Started>()
            with(currentState) {
                // todo почему -1 это вообще валидное значение? попробовать использовать unsigned тип данных
                require(testMetadata.dataSetNumber != null && testMetadata.dataSetNumber != -1) {
                    "Please specify @DataSetNumber(Int) for test ${testMetadata.testName}"
                }
                dataSet = value
            }
        }
    }

    @Synchronized
    override fun stepFailed(exception: StepException) {
        registerIncident(exception, "Screenshot после падения step")
    }

    @Synchronized
    override fun preconditionFailed(exception: StepException) {
        registerIncident(exception, "Screenshot после падения precondition")
    }

    @Synchronized
    override fun addScreenshot(label: String) {
        methodExecutionTracing("addScreenshot") {
            val futureResult: Result<FutureValue<RemoteStorage.Result>?> =
                screenshotCapturer.captureAsFile().map { screenshot: File? ->
                    if (screenshot == null) {
                        // no resumed activity, can't capture
                        null
                    } else {
                        val initialized = getCastedState<ReportState.Initialized>()

                        transport.sendContent(
                            test = initialized.testMetadata,
                            request = RemoteStorage.Request.FileRequest.Image(screenshot),
                            comment = label
                        )
                    }
                }

            futureResult.fold(
                { futureValue ->
                    if (futureValue != null) {
                        updateStep {
                            futureUploads.add(futureValue)
                        }
                    }
                },
                { throwable ->
                    logger.warn("Failed to update step with captured screenshot", throwable)
                }
            )
        }
    }

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
    override fun startPrecondition(step: StepResult): Unit =
        methodExecutionTracing("startPrecondition") {
            val currentState = getCastedState<ReportState.Initialized.Started>()
            val currentStep = currentState.currentStep
            require(currentStep == null || currentStep.isSynthetic) {
                "Can't start precondition \"${step.title}\" when another one exists: \"${currentStep?.title}\"." +
                    "Preconditions inside steps are not supported."
            }
            currentState.currentStep = step
            beforePreconditionStart(step)
        }

    @Synchronized
    override fun stopPrecondition(): Unit = methodExecutionTracing("stopPrecondition") {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Can't stop precondition because it isn't started"
        }
        currentState.preconditionStepList.add(currentStep)
        afterPreconditionStop(currentStep)
        currentState.currentStep = null
    }

    @Synchronized
    override fun startStep(step: StepResult): Unit = methodExecutionTracing("startStep") {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        val currentStep = currentState.currentStep
        require(currentStep == null || currentStep.isSynthetic) {
            "Can't start step \"${step.title}\" when another one exists: \"${currentStep?.title}\". " +
                "Nested steps are not supported."
        }
        currentState.currentStep = step
        beforeStepStart(step)
    }

    @Synchronized
    override fun stopStep(): Unit = methodExecutionTracing("stopStep") {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Can't stop step because it isn't started"
        }
        currentState.testCaseStepList.add(currentStep)
        afterStepStop(currentStep)
        currentState.currentStep = null
    }

    @Synchronized
    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        methodExecutionTracing("addHtml") {
            val initialized = getCastedState<ReportState.Initialized>()

            val wrappedContentIfNeeded = if (wrapHtml) wrapInHtml(content) else content

            val html = transport.sendContent(
                test = initialized.testMetadata,
                request = RemoteStorage.Request.ContentRequest.Html(
                    content = wrappedContentIfNeeded,
                ),
                comment = label
            )

            val started = getCastedStateOrNull<ReportState.Initialized.Started>()
            val futureUploads = started
                ?.currentStepOrSynthetic()
                ?.futureUploads ?: earlyFuturesUploads
            futureUploads.add(html)
        }
    }

    @Synchronized
    override fun addText(label: String, text: String) {
        methodExecutionTracing("addText") {
            val initialized = getCastedState<ReportState.Initialized>()

            val txt = transport.sendContent(
                test = initialized.testMetadata,
                request = RemoteStorage.Request.ContentRequest.PlainText(
                    content = text,
                ),
                comment = label
            )
            val started = getCastedStateOrNull<ReportState.Initialized.Started>()
            val futureUploads = started
                ?.currentStepOrSynthetic()
                ?.futureUploads ?: earlyFuturesUploads
            futureUploads.add(txt)
        }
    }

    @Synchronized
    override fun addComment(comment: String) {
        methodExecutionTracing("addComment") {
            addEntry(Entry.Comment(comment, timeProvider.nowInSeconds()))
        }
    }

    @Synchronized
    override fun addAssertion(label: String) {
        methodExecutionTracing("addAssertion") {
            addEntry(Entry.Check(label, timeProvider.nowInSeconds()))
        }
    }

    private fun createIncidentChainFactory() = IncidentChainFactory.Impl(
        customViewPresenters = setOf(
            TestCaseIncidentPresenter(),
            RequestIncidentPresenter(),
            ResourceIncidentPresenter(),
            ResourceManagerIncidentPresenter(),
            AppCrashIncidentPresenter()
        ),
        fallbackPresenter = FallbackIncidentPresenter()
    )

    private fun makeScreenshot(comment: String): Result<FutureValue<RemoteStorage.Result>?> =
        methodExecutionTracing("makeScreenshot") {
            screenshotCapturer.captureAsFile().map { screenshot: File? ->
                if (screenshot == null) {
                    // no resumed activity, can't capture
                    null
                } else {
                    val initialized = getCastedState<ReportState.Initialized>()

                    transport.sendContent(
                        test = initialized.testMetadata,
                        request = RemoteStorage.Request.FileRequest.Image(screenshot),
                        comment = comment
                    )
                }
            }
        }

    private fun updateStep(update: StepResult.() -> Unit) {
        val currentState = getCastedState<ReportState.Initialized.Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Couldn't update step because it hasn't started yet"
        }

        beforeStepUpdate(currentStep)
        currentStep.update()
        afterStepUpdate(currentStep)
    }

    private fun addEntry(entry: Entry) {
        val started = getCastedStateOrNull<ReportState.Initialized.Started>()
        val entriesList = started
            ?.currentStepOrSynthetic()
            ?.entryList ?: earlyEntries
        entriesList.add(entry)
    }

    private fun ReportState.Initialized.Started.currentStepOrSynthetic() =
        getCurrentStepOrCreate {
            StepResult(
                isSynthetic = true,
                timestamp = timeProvider.nowInSeconds(),
                number = stepNumber++,
                title = "Out of step"
            )
        }

    private fun Throwable.determineIncidentType(): Incident.Type {
        return when {
            else -> when (this) {
                is AssertionError, is EspressoException -> Incident.Type.ASSERTION_FAILED
                else -> cause?.determineIncidentType() ?: Incident.Type.INFRASTRUCTURE_ERROR
            }
        }
    }

    private inline fun <reified T : ReportState> checkStateIs() = getCastedState<T>()

    private inline fun <reified T : ReportState> getCastedStateOrNull(): T? {
        return when (val localState = state) {
            is T -> localState
            else -> null
        }
    }

    private inline fun <reified T : ReportState> getCastedState(cause: Throwable? = null): T {
        val castedClass = getCastedStateOrNull<T>()

        if (castedClass != null) {
            return castedClass
        } else {
            val localState = state

            val detailedCause = when {
                cause != null -> cause

                localState is ReportState.Initialized ->
                    IllegalStateException("Test not started, incident = ${localState.incident}")

                else -> null
            }

            throw IllegalStateException(
                "Invalid state. Expected ${T::class.simpleName} actual ${state::class.java.simpleName}",
                detailedCause
            )
        }
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
            .map {
                Entry.File(
                    comment = it.comment,
                    timeInSeconds = it.timeInSeconds,
                    fileType = it.uploadRequest.toFileType(),
                    fileAddress = it.fileAddress()
                )
            }
            .toList()

    private fun RemoteStorage.Result.fileAddress(): FileAddress {
        // false positive 'must be exhaustive' error in IDE,
        // should be fixed in kotlin 1.5 https://youtrack.jetbrains.com/issue/KT-44821
        return when (this) {
            is RemoteStorage.Result.Success -> try {
                FileAddress.URL(url.toHttpUrl())
            } catch (e: IllegalArgumentException) {
                FileAddress.Error(e)
            }
            is RemoteStorage.Result.Error -> FileAddress.Error(t)
        }
    }

    private fun RemoteStorage.Request.toFileType(): Entry.File.Type {
        // false positive 'must be exhaustive' error in IDE,
        // should be fixed in kotlin 1.5 https://youtrack.jetbrains.com/issue/KT-44821
        return when (this) {
            is RemoteStorage.Request.ContentRequest.Html -> Entry.File.Type.html
            is RemoteStorage.Request.ContentRequest.PlainText -> Entry.File.Type.plain_text
            is RemoteStorage.Request.FileRequest.Image -> Entry.File.Type.img_png
            is RemoteStorage.Request.FileRequest.Video -> Entry.File.Type.video
        }
    }

    private fun ReportState.Initialized.Started.writeTestCase() =
        methodExecutionTracing("writeTestCase") {
            beforeTestWrite(this)
            transport.sendReport(this)
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
