
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.test.report.ReportImplementation
import com.avito.android.test.report.ReportState

class InfrastructureTestRule(
    private val assertionBlock: (ReportState.Initialized.Started) -> Unit
) : SimpleRule() {

    override fun after() {
        assertionBlock(getStartedReportStateOrThrow())
    }

    private fun getStartedReportStateOrThrow(): ReportState.Initialized.Started {
        val report = InHouseInstrumentationTestRunner.instance.report as ReportImplementation
        return report.currentState as? ReportState.Initialized.Started
            ?: throw IllegalStateException("Report state must be Initialized.Started during test execution")
    }
}
