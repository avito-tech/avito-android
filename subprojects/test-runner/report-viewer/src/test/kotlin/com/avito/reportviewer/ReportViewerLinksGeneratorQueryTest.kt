package com.avito.reportviewer

import com.avito.report.model.Team
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReportViewerLinksGeneratorQueryTest {

    @Test
    fun test() {
        val query = ReportViewerQuery.createForJvm()
            .createQuery(onlyFailures = true, team = Team("messenger"))

        assertThat(query)
            .isEqualTo("?q=eyJmaWx0ZXIiOnsic3VjY2VzcyI6MCwic2tpcCI6MCwiZ3JvdXBzIjpbIm1lc3NlbmdlciJdfX0=")
    }
}
