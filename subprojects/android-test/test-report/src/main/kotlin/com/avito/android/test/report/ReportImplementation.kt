package com.avito.android.test.report

import androidx.annotation.VisibleForTesting
import androidx.test.espresso.EspressoException
import com.avito.android.Result
import com.avito.android.test.report.ReportState.NotFinished
import com.avito.android.test.report.ReportState.NotFinished.Initialized
import com.avito.android.test.report.ReportState.NotFinished.Initialized.NotStarted
import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.ReportState.NotFinished.NotInitialized
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
import com.avito.report.model.Incident
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList
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

    private var state: ReportState = NotInitialized()

    @VisibleForTesting
    val currentState: ReportState
        get() = state

    override val isFirstStep: Boolean
        get() = getCastedStateOrNull<Started>()
            ?.isFirstStepOrPrecondition ?: false

    private val isFinished: Boolean
        get() = currentState is ReportState.Finished

    @Synchronized
    override fun createStepModel(stepName: String): StepResult {
        val currentState = getCastedState<Started>()
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = currentState.stepNumber++
        )
    }

    @Synchronized
    override fun createPreconditionModel(stepName: String): StepResult {
        val currentState = getCastedState<Started>()
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = currentState.preconditionNumber++
        )
    }

    @Synchronized
    override fun unexpectedFailedTestCase(exception: Throwable) {
        if (!isFinished) {
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
        val currentState = getCastedState<Initialized>(cause = exception)

        if (currentState.incident == null) {
            if (screenshot != null) {
                currentState.incidentScreenshot = screenshot
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
            val startedState = getCastedState<Started>()
            startedState.endTime = timeProvider.nowInSeconds()

            try {
                afterTestStop(startedState)
            } catch (t: Throwable) {
                logger.warn("Failed while afterTestStop were executing", t)
            }

            with(startedState) {
                waitUploads()
                finishTestCase()
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
            val currentState = getCastedState<Started>()
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
                screenshotCapturer.captureAsFile().map { screenshot: File ->
                    val initialized = getCastedState<Initialized>()

                    transport.sendContent(
                        test = initialized.testMetadata,
                        request = RemoteStorage.Request.FileRequest.Image(screenshot),
                        comment = label
                    )
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
        val currentState = getCastedState<NotInitialized>()
        state = NotStarted(
            entriesBeforeSteps = currentState.entriesBeforeSteps,
            uploadsBeforeSteps = currentState.uploadsBeforeSteps,
            testMetadata = testMetadata
        )
    }

    @Synchronized
    override fun startTestCase(): Unit = methodExecutionTracing("startTestCase") {
        val currentState = getCastedState<NotStarted>()

        val started = Started(
            entriesBeforeSteps = currentState.entriesBeforeSteps,
            uploadsBeforeSteps = currentState.uploadsBeforeSteps,
            testMetadata = currentState.testMetadata,
            startTime = timeProvider.nowInSeconds()
        )
        state = started
        beforeTestStart(started)
    }

    @Synchronized
    override fun startPrecondition(step: StepResult): Unit =
        methodExecutionTracing("startPrecondition") {
            val currentState = getCastedState<Started>()
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
        val currentState = getCastedState<Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Can't stop precondition because it isn't started"
        }
        currentState.preconditionStepList.add(currentStep)
        afterPreconditionStop(currentStep)
        currentState.currentStep = null
    }

    @Synchronized
    override fun startStep(step: StepResult): Unit = methodExecutionTracing("startStep") {
        val currentState = getCastedState<Started>()
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
        val currentState = getCastedState<Started>()
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
            val initialized = getCastedState<Initialized>()

            val wrappedContentIfNeeded = if (wrapHtml) wrapInHtml(content) else content

            val html = transport.sendContent(
                test = initialized.testMetadata,
                request = RemoteStorage.Request.ContentRequest.Html(
                    content = wrappedContentIfNeeded,
                ),
                comment = label
            )

            val started = getCastedStateOrNull<Started>()
            val futureUploads = started
                ?.currentStepOrSynthetic()
                ?.futureUploads ?: initialized.uploadsBeforeSteps
            futureUploads.add(html)
        }
    }

    @Synchronized
    override fun addText(label: String, text: String) {
        methodExecutionTracing("addText") {
            val initialized = getCastedState<Initialized>()

            val txt = transport.sendContent(
                test = initialized.testMetadata,
                request = RemoteStorage.Request.ContentRequest.PlainText(
                    content = text,
                ),
                comment = label
            )
            val started = getCastedStateOrNull<Started>()
            val futureUploads = started
                ?.currentStepOrSynthetic()
                ?.futureUploads ?: initialized.uploadsBeforeSteps
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
            screenshotCapturer.captureAsFile().map { screenshot: File ->
                val initialized = getCastedState<Initialized>()

                transport.sendContent(
                    test = initialized.testMetadata,
                    request = RemoteStorage.Request.FileRequest.Image(screenshot),
                    comment = comment
                )
            }
        }

    private fun updateStep(update: StepResult.() -> Unit) {
        val currentState = getCastedState<Started>()
        val currentStep = requireNotNull(currentState.currentStep) {
            "Couldn't update step because it hasn't started yet"
        }

        beforeStepUpdate(currentStep)
        currentStep.update()
        afterStepUpdate(currentStep)
    }

    private fun addEntry(entry: Entry) {
        val started = getCastedStateOrNull<Started>()
        val entriesList = started
            ?.currentStepOrSynthetic()
            ?.entryList ?: getCastedState<NotFinished>().entriesBeforeSteps
        entriesList.add(entry)
    }

    private fun Started.currentStepOrSynthetic() =
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

                localState is Initialized ->
                    IllegalStateException("Test not started, incident = ${localState.incident}")

                else -> null
            }

            throw IllegalStateException(
                "Invalid state. Expected ${T::class.simpleName} actual ${state::class.java.simpleName}",
                detailedCause
            )
        }
    }

    private fun Started.finishTestCase() =
        methodExecutionTracing("finishTestCase") {
            beforeTestWrite(this)
            transport.sendReport(this)
            state = ReportState.Finished
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
