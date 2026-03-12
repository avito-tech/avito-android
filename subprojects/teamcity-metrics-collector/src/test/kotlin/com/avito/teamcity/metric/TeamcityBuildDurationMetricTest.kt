package com.avito.teamcity.metric

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TeamcityBuildDurationMetricTest {

    @Test
    fun `resulting parameters with metric tag prefix - asGraphite - should include tags in metric path`() {
        val build = StubBuild(
            resultingParameters = listOf(
                StubParameter("metadata.metrics.tag.tag_one", "value_one"),
                StubParameter("metadata.metrics.tag.tag_two", "value_two"),
                StubParameter("unrelated.param", "ignored"),
            )
        )

        val metric = TeamcityBuildDurationMetric(build).asGraphite()

        assertThat(metric.path.toString()).contains("tag_one=value_one")
        assertThat(metric.path.toString()).contains("tag_two=value_two")
        assertThat(metric.path.toString()).doesNotContain("unrelated")
    }

    @Test
    fun `resulting parameters with blank value - asGraphite - should skip blank tags`() {
        val build = StubBuild(
            resultingParameters = listOf(
                StubParameter("metadata.metrics.tag.valid", "value"),
                StubParameter("metadata.metrics.tag.empty", ""),
                StubParameter("metadata.metrics.tag.blank", "   "),
            )
        )

        val metric = TeamcityBuildDurationMetric(build).asGraphite()

        assertThat(metric.path.toString()).contains("valid=value")
        assertThat(metric.path.toString()).doesNotContain("empty")
        assertThat(metric.path.toString()).doesNotContain("blank")
    }

    @Test
    fun `resulting parameters with null value - asGraphite - should skip null tags`() {
        val build = StubBuild(
            resultingParameters = listOf(
                StubParameter("metadata.metrics.tag.valid", "value"),
                StubParameter("metadata.metrics.tag.nullable", null),
            )
        )

        val metric = TeamcityBuildDurationMetric(build).asGraphite()

        assertThat(metric.path.toString()).contains("valid=value")
        assertThat(metric.path.toString()).doesNotContain("nullable")
    }

    @Test
    fun `no resulting parameters with prefix - asGraphite - should have no additional tags`() {
        val build = StubBuild(
            resultingParameters = listOf(
                StubParameter("unrelated.param", "value"),
            )
        )

        val metric = TeamcityBuildDurationMetric(build).asGraphite()

        assertThat(metric.path.toString())
            .isEqualTo("teamcity.build;build_type=TestBuild;status=success")
    }
}
