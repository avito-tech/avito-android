package com.avito.android.plugin.build_metrics.internal.runtime.jvm

internal sealed class LocalVm(
    /**
     * Local VM identifier.
     * Typically, but not necessarily, the PID.
     */
    val id: Long
) {

    class GradleDaemon(id: Long) : LocalVm(id)

    class GradleWorker(id: Long) : LocalVm(id)

    class KotlinDaemon(id: Long) : LocalVm(id)

    class Unknown(
        id: Long,
        /**
         * classname | JAR filename | "Unknown"
         */
        val name: String
    ) : LocalVm(id) {

        override fun toString(): String {
            return "JVM($id, $name)"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalVm

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "${javaClass.simpleName}($id)"
    }
}
