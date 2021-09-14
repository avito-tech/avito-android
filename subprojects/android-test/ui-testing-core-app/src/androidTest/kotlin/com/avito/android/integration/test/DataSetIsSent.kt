package com.avito.android.integration.test

import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.report.dataSet
import com.avito.android.test.report.model.DataSet
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test

@CaseId(ReportTestUtils.SPECIAL_TEST_CASE_ID)
@IntegrationTest
class DataSetIsSent {

    @get:Rule
    val testCase = InfrastructureTestRule {
        assertWithMessage(
            "Field data_set must be equal to the com.avito.android.test.report.StepKt.dataSet value argument"
        )
            .that(it.dataSet)
            .isEqualTo(DataSetImpl("messageOne"))

        assertWithMessage(
            "Field dataSetNumber must be taken from com.avito.android.test.annotations.DataSetNumber"
        )
            .that(it.testMetadata.dataSetNumber)
            .isEqualTo(1)
    }

    fun test(@Suppress("UNUSED_PARAMETER") dataSetValue: DataSetImpl) {
        simpleSuccessAssertion()
    }

    @DataSetNumber(1)
    @Test
    fun dataSet1() {
        dataSet(DataSetImpl("messageOne")) { test(it) }
    }

    data class DataSetImpl(val message: String) : DataSet
}
