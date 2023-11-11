package com.avito.reportviewer.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class RunIdTest {

    @Test
    fun `serialize`() {
        val actual = RunId(identifier = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID).toReportViewerFormat()
        val expected = "$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID"
        assertThat(actual).isEqualTo(expected)
    }

    private companion object {
        const val COMMIT_HASH = "8982d2d2"
        const val BUILD_TYPE_ID = "local"
    }
}
