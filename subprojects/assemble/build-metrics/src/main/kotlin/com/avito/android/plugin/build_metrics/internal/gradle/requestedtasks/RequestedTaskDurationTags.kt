package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.avito.graphite.series.SeriesName

internal object RequestedTaskDurationTags {

    private val BASE = SeriesName.create("gradle.requested_task.duration", multipart = true)

    fun buildSeriesName(
        taskName: String,
        userName: String,
        isInvokedFromIde: Boolean,
        ramGb: Int,
        isCommitChanged: Boolean,
    ): SeriesName {
        val tags = mapOf(
            "task_name" to taskName,
            "user_name" to userName,
            "is_invoked_from_ide" to isInvokedFromIde.toString(),
            "ram_gb" to ramGb.toString(),
            "commit_changed" to isCommitChanged.toString(),
        )
        return BASE.addTags(tags)
    }
}
