package com.avito.android.integration.test

import InfrastructureTestRule
import com.avito.android.integration.test.ReportTestUtils.simpleSuccessAssertion
import com.avito.android.test.annotations.Description
import com.avito.android.test.annotations.ExternalId
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.Priority
import com.avito.android.test.annotations.TagId
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.util.Is
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
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
        assertThat(
            "Поле externalId должно быть взято из аннотации",
            startedReportState.testMetadata.externalId,
            Is("6faac31e-655c-4ac2-b18c-ede8e194e472")
        )
        assertThat(
            "Поле tagId должно быть взято из аннотации",
            startedReportState.testMetadata.tagIds,
            hasItems(-1, -2, -3)
        )
        assertThat(
            "Поле description должно быть взято из аннотации",
            startedReportState.testMetadata.description,
            Is("annotation_data_sent")
        )
    }

    @Test
    fun test() {
        simpleSuccessAssertion()
    }
}
