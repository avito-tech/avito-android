package com.avito.android.integration.test

import InfrastructureTestRule
import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.util.Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

@IntegrationTest
class DataFromAnnotationMethodIsSent {

    @get:Rule
    val testCase = InfrastructureTestRule {
        assertThat(
            "Field description must be taken from com.avito.android.test.annotations.Description",
            it.testMetadata.description,
            Is("annotation_in_method_data_sent")
        )
    }

    @Test
    @Description("annotation_in_method_data_sent")
    fun test() {
        simpleSuccessAssertion()
    }
}
