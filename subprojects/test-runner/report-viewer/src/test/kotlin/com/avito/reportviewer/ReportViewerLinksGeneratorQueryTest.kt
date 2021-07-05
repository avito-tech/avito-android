package com.avito.reportviewer

import com.avito.report.model.Team
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReportViewerLinksGeneratorQueryTest {

    @Test
    fun test() {
        assertThat(ReportViewerQuery().createQuery(onlyFailures = true, team = Team("messenger")))
            .isEqualTo("?q=eyJmaWx0ZXIiOnsic3VjY2VzcyI6MCwic2tpcCI6MCwiZ3JvdXBzIjpbIm1lc3NlbmdlciJdfX0=")
    }
}
