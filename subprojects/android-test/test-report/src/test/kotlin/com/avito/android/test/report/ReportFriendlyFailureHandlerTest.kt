package com.avito.android.test.report

import androidx.test.espresso.NoMatchingViewException
import com.avito.android.util.fileFromJarResources
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ReportFriendlyFailureHandlerTest {

    @Test
    fun `failureHandler - removes view hierarchy`() {
        val exception = assertThrows<NoMatchingViewException> {
            ReportFriendlyFailureHandler(StubFailureHandler).handle(
                error = createExceptionWithPrivateStringConstructor<NoMatchingViewException>(
                    fileFromJarResources<ReportFriendlyFailureHandlerTest>("view-hierarchy.txt").readText()
                ),
                viewMatcher = null
            )
        }

        assertThat(exception.message).isEqualTo(
            "Не найдена view в иерархии: \"(is descendant of a: " +
                "with id: com.avito.android.stagingautotest:id/layout_delivery_buttons and with id: 2131363381)\""
        )
    }
}
