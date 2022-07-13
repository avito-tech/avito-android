package com.avito.android.plugin.build_metrics.internal.jvm

internal sealed class LocalVm(
    /**
     * Local VM identifier.
     * Typically, but not necessarily, the PID.
     */
    val id: Long,
    /**
     * The lower value - the shorter expected duration of process live
     */
    val longevityRank: Int
) {

    class GradleDaemon(id: Long) : LocalVm(id, longevityRank = 3)

    class GradleWorker(id: Long) : LocalVm(id, longevityRank = 1)

    class KotlinDaemon(id: Long) : LocalVm(id, longevityRank = 2)

    class Unknown(
        id: Long,
        /**
         * classname | JAR filename | "Unknown"
         */
        val name: String
    ) : LocalVm(id, longevityRank = 1) {

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
