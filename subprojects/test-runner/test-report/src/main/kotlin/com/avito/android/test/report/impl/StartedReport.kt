package com.avito.android.test.report.impl

import androidx.test.espresso.EspressoException
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.ReportStepModelFactory
import com.avito.android.test.report.StepException
import com.avito.android.test.report.incident.AppCrashException
import com.avito.android.test.report.incident.AppCrashIncidentPresenter
import com.avito.android.test.report.incident.FallbackIncidentPresenter
import com.avito.android.test.report.incident.IncidentChainFactory
import com.avito.android.test.report.incident.RequestIncidentPresenter
import com.avito.android.test.report.incident.ResourceIncidentPresenter
import com.avito.android.test.report.incident.ResourceManagerIncidentPresenter
import com.avito.android.test.report.incident.TestCaseIncidentPresenter
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepAttachments
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.screenshot.ScreenshotCapturer
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.time.TimeProvider
import com.avito.utils.stackTraceToList

internal class StartedReport(
    loggerFactory: LoggerFactory,
    transport: Transport,
    screenshotCapturer: ScreenshotCapturer,
    timeProvider: TimeProvider,
    private val troubleshooter: Troubleshooter,
    override val state: ReportState.NotFinished.Initialized.Started,
) : BaseInitializedReport(loggerFactory, transport, screenshotCapturer, timeProvider),
    ReportStepModelFactory<StepResult>,
    TestLifecycleListener by TestLifecycleNotifier {

    private val logger = loggerFactory.create<StartedReport>()

    override val currentState: ReportState = state

    override val isFirstStep: Boolean
        get() = state.isFirstStepOrPrecondition

    override val currentAttachments: StepAttachments
        get() = currentStepOrSynthetic().attachments

    private fun currentStepOrSynthetic() =
        with(state) {
            getCurrentStepOrCreate {
                StepResult(
                    isSynthetic = true,
                    timestamp = timeProvider.nowInSeconds(),
                    number = stepNumber++,
                    title = "Out of step"
                )
            }
        }

    override fun createStepModel(stepName: String): StepResult {
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = state.stepNumber++
        )
    }

    override fun createPreconditionModel(stepName: String): StepResult {
        return StepResult(
            isSynthetic = false,
            title = stepName,
            timestamp = timeProvider.nowInSeconds(),
            number = state.preconditionNumber++
        )
    }

    /**
     * [unexpectedFailedTestCase] can be called in any moment and it isn't connected to test lifecycle
     * So we must call [finishTestCase] directly
     */
    override fun unexpectedFailedTestCase(exception: Throwable) {
        registerIncident(AppCrashException(exception), null)
        finishTestCase()
    }

    override fun startTestCase() {
        beforeTestStart(state)
    }

    override fun finishTestCase() {
        state.endTime = timeProvider.nowInSeconds()

        try {
            beforeTestFinished(state)
        } catch (t: Throwable) {
            logger.warn("Failed while afterTestStop were executing", t)
        }

        with(state) {
            waitUploads()
            transport.sendReport(this)
            afterTestFinished(this)
        }
    }

    override fun failedTestCase(exception: Throwable) {
        registerIncident(exception, "Failed test case")
    }

    override fun setDataSet(value: DataSet) {
        with(state) {
            // todo почему -1 это вообще валидное значение? попробовать использовать unsigned тип данных
            require(testMetadata.dataSetNumber != null && testMetadata.dataSetNumber != -1) {
                "Please specify @DataSetNumber(Int) for test ${testMetadata.name}"
            }
            dataSet = value
        }
    }

    override fun stepFailed(exception: StepException) {
        registerIncident(exception, "Screenshot после падения step")
    }

    override fun preconditionFailed(exception: StepException) {
        registerIncident(exception, "Screenshot после падения precondition")
    }

    override fun startPrecondition(step: StepResult) {
        val currentStep = state.currentStep
        require(currentStep == null || currentStep.isSynthetic) {
            "Can't start precondition \"${step.title}\" when another one exists: \"${currentStep?.title}\"." +
                "Preconditions inside steps are not supported."
        }
        state.currentStep = step
    }

    override fun stopPrecondition() {
        val currentStep = requireNotNull(state.currentStep) {
            "Can't stop precondition because it isn't started"
        }
        state.preconditionStepList.add(currentStep)
        state.currentStep = null
    }

    override fun startStep(step: StepResult) {
        val currentStep = state.currentStep
        require(currentStep == null || currentStep.isSynthetic) {
            "Can't start step \"${step.title}\" when another one exists: \"${currentStep?.title}\". " +
                "Nested steps are not supported."
        }
        state.currentStep = step
    }

    override fun stopStep() {
        val currentStep = requireNotNull(state.currentStep) {
            "Can't stop step because it isn't started"
        }
        state.testCaseStepList.add(currentStep)
        state.currentStep = null
    }

    private fun registerIncident(
        exception: Throwable,
        screenshotName: String
    ) {
        registerIncident(exception, makeScreenshot(screenshotName).getOrElse { null })
    }

    private fun registerIncident(
        exception: Throwable,
        screenshot: FutureValue<Entry.File>?
    ) {

        if (state.incident == null) {
            if (screenshot != null) {
                state.incidentScreenshot = screenshot
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
            state.incident = incidentToAdd
            afterIncident(incidentToAdd)
        } else {
            logger.warn("Fail to register incident. Incident already exist", exception)
        }
    }

    private fun addTroubleshootingEntries() {
        troubleshooter.troubleshootTo(this)
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

    private fun Throwable.determineIncidentType(): Incident.Type {
        return when {
            else -> when (this) {
                is AssertionError, is EspressoException -> Incident.Type.ASSERTION_FAILED
                else -> cause?.determineIncidentType() ?: Incident.Type.INFRASTRUCTURE_ERROR
            }
        }
    }
}
