package com.avito.android.critical_path

import com.avito.graph.Operation
import org.gradle.api.Task
import org.gradle.util.Path

public data class TaskOperation(
    val path: Path,
    val type: Class<out Task>,
    val startMs: Long,
    val finishMs: Long,
    override val predecessors: Set<String>
) : Operation {

    override val id: String
        get() = path.toString()

    override val duration: Double
        get() = (finishMs - startMs).toDouble()

    val durationMs: Long
        get() = finishMs - startMs

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
}
