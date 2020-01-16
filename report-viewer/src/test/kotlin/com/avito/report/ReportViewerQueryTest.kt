package com.avito.report

import com.avito.report.model.Team
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReportViewerQueryTest {

    @Test
    fun test() {
        assertThat(ReportViewerQuery().createQuery(onlyFailures = true, team = Team("messenger")))
            .isEqualTo("?q=eyJmaWx0ZXIiOnsiZXJyb3IiOjEsImZhaWwiOjEsIm90aGVyIjoxLCJncm91cHMiOlsibWVzc2VuZ2VyIl19fQ==")
    }
}
