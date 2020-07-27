package com.avito.android.integration.test

import com.avito.android.integration.test.ReportTestUtils.SPECIAL_TEST_CASE_ID
import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.IntegrationTest
import org.junit.Test


@CaseId(SPECIAL_TEST_CASE_ID)
@Description("report_test")
@IntegrationTest
class SimpleTestIsSuccess {

    @Test
    fun test() {
        simpleSuccessAssertion()
    }
}
