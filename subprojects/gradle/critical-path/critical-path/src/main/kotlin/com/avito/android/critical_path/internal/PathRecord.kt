package com.avito.android.critical_path.internal

import com.avito.android.critical_path.TaskOperation
import com.google.gson.annotations.SerializedName

internal class PathRecord(

    @SerializedName("path")
    val path: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("start")
    val startMs: Long,

    @SerializedName("finish")
    val finishMs: Long,
) {

    companion object {

        fun fromTaskOperation(task: TaskOperation): PathRecord {
            return PathRecord(
                path = task.path.toString(),
                type = task.type.name,
                startMs = task.startMs,
                finishMs = task.finishMs,
            )
        }
    }
}
