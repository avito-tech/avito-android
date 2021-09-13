package com.avito.android.integration.test

import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.ExternalId
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCasePriority
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test

@Description("annotation_data_sent")
@Priority(TestCasePriority.MAJOR)
@ExternalId("6faac31e-655c-4ac2-b18c-ede8e194e472")
@TagId([-1, -2, -3])
@IntegrationTest
class DataFromAnnotationIsSent {

    @get:Rule
    val testCase = InfrastructureTestRule { startedReportState ->
        assertWithMessage("Field externalId must be taken from com.avito.android.test.annotations.ExternalId")
            .that(startedReportState.testMetadata.externalId)
            .isEqualTo("6faac31e-655c-4ac2-b18c-ede8e194e472")

        assertWithMessage("Field tagId must be taken from com.avito.android.test.annotations.TagId")
            .that(startedReportState.testMetadata.tagIds)
            .containsExactly(-1, -2, -3)

        assertWithMessage("Field description must be taken from com.avito.android.test.annotations.Description")
            .that(startedReportState.testMetadata.description)
            .isEqualTo("annotation_data_sent")
    }

    @Test
    fun test() {
        simpleSuccessAssertion()
    }
}
