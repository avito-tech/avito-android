package com.avito.android.integration.test

import InfrastructureTestRule
import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.report.dataSet
import com.avito.android.test.report.model.DataSet
import com.avito.android.util.Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

@CaseId(ReportTestUtils.SPECIAL_TEST_CASE_ID)
@IntegrationTest
class DataSetIsSent {

    @get:Rule
    val testCase = InfrastructureTestRule {
        assertThat(
            "Поле data_set должно содержать поля объекта переданного в тест",
            it.dataSet,
            Is<DataSet>(DataSetImpl("messageOne"))
        )

        assertThat(
            "Поле data_set должно содержать поля объекта переданного в тест",
            it.testMetadata.dataSetNumber,
            Is(1)
        )
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
