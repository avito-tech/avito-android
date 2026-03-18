package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric

internal class RequestedTaskDurationMetric(
    private val taskName: String,
    private val userName: String,
    private val isInvokedFromIde: Boolean,
    private val ramGb: Int,
    private val isCommitChanged: Boolean,
    private val durationMs: Long,
) : BuildMetric.Graphite() {

    override fun asGraphite(): GraphiteMetric {
        val series = RequestedTaskDurationTags.buildSeriesName(
            taskName = taskName,
            userName = userName,
            isInvokedFromIde = isInvokedFromIde,
            ramGb = ramGb,
            isCommitChanged = isCommitChanged,
        )
        return GraphiteMetric(series, durationMs.toString())
    }
}
