package com.avito.instrumentation.rerun

import com.avito.instrumentation.report.Report
import com.avito.report.FakeReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.avito.utils.logging.FakeCILogger
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class MergeResultsWithTargetBranchRunTest {

    private val reportsApi = FakeReportsApi()
    private val logger = FakeCILogger()
    private val reportCoordinates = ReportCoordinates.createStubInstance()
    private val report = Report.Impl(reportsApi, logger, reportCoordinates, "stub")
    private val merger = MergeResultsWithTargetBranchRun(logger, report)

    /**
     * Пока мы просто маркем в любом случае, дальше в случае false positives нужно будет полагаться на verdict/errorHash
     */
    @Test
    fun `merge - marks test as successful - after failure on rerun with different errors`() {
        merger.merge(
            Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        id = "12345",
                        name = "testOne",
                        status = Status.Failure("error", "1")
                    )
                )
            ),
            Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        id = "12345",
                        name = "testOne",
                        status = Status.Failure("another problem", "2")
                    )
                )
            )
        )

        assertThat(reportsApi.getLastMarkAsSuccessfulRequest()).isNotNull()
        assertThat(reportsApi.getLastMarkAsSuccessfulRequest()!!.testRunId).isEqualTo("12345")
        assertThat(reportsApi.getLastMarkAsSuccessfulRequest()!!.comment).contains("testOne error hashes are different")
    }

    @Test
    fun `merge - does nothing - on different test failures`() {
        merger.merge(
            Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        name = "testOne",
                        status = Status.Failure("error", "1")
                    )
                )
            ),
            Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        name = "testTwo",
                        status = Status.Failure("error", "1")
                    )
                )
            )
        )

        assertThat(reportsApi.getLastMarkAsSuccessfulRequest()).isNull()
    }
}
