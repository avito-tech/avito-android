package com.avito.android.integration.test

import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.IntegrationTest
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test

@IntegrationTest
class DataFromAnnotationMethodIsSent {

    @get:Rule
    val testCase = InfrastructureTestRule {
        assertWithMessage("Field description must be taken from com.avito.android.test.annotations.Description")
            .that(it.testMetadata.description)
            .isEqualTo("annotation_in_method_data_sent")
    }

    @Test
    @Description("annotation_in_method_data_sent")
    fun test() {
        simpleSuccessAssertion()
    }
}
