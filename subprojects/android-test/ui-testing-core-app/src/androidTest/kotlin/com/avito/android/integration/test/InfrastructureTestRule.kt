
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started

class InfrastructureTestRule(
    private val assertionBlock: (Started) -> Unit
) : SimpleRule() {

    override fun after() {
        assertionBlock(getStartedReportStateOrThrow())
    }

    private fun getStartedReportStateOrThrow(): Started {
        val state = InHouseInstrumentationTestRunner.instance.report.currentState
        return state as? Started
            ?: throw IllegalStateException("Report state must be Initialized.Started during test execution")
    }
}
