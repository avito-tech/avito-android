package com.avito.android.build_trace.internal.critical_path

import com.avito.android.build_trace.internal.predecessors
import com.avito.android.build_trace.internal.type
import com.avito.android.gradle.profile.TaskExecution
import com.avito.graph.Operation
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.gradle.api.Task

internal data class TaskOperation(

    @Expose
    @SerializedName("path")
    val path: String,

    @Expose
    @SerializedName("type")
    val type: String,

    @Expose
    @SerializedName("start")
    val startMs: Long,

    @Expose
    @SerializedName("finish")
    val finishMs: Long,

    @Expose
    override val predecessors: Set<String>
) : Operation {

    override val id: String
        get() = path

    override val duration: Double
        get() = (finishMs - startMs).toDouble()

    override fun toString(): String {
        return "TaskOperation($path)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Operation) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {

        fun from(task: Task, state: TaskExecution): TaskOperation {
            return TaskOperation(
                path = task.path,
                type = task.type.name,
                startMs = state.startTime,
                finishMs = state.finish,
                predecessors = task.predecessors.map { it.path }.toSet()
            )
        }
    }
}
