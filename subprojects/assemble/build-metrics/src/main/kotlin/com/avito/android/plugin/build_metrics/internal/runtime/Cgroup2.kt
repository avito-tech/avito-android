package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.Cgroup2.Available.Memory
import org.gradle.internal.os.OperatingSystem
import java.io.File

/**
 * [Control group](https://www.kernel.org/doc/html/latest/admin-guide/cgroup-v2.html)
 */
internal sealed class Cgroup2 {

    object Unavailable : Cgroup2()

    class Available(
        val memory: Memory
    ) : Cgroup2() {

        /**
         * [control group - memory](https://www.kernel.org/doc/html/latest/admin-guide/cgroup-v2.html#memory)
         */
        class Memory(
            private val memoryMax: File,
            private val memoryCurrent: File,
        ) {

            val memoryMaxBytes: Long by lazy {
                memoryMax.readText().trim().toLong()
            }

            val memoryCurrentBytes: Long
                get() = memoryCurrent.readText().trim().toLong()
        }
    }

    companion object {

        fun resolve(): Cgroup2 {
            if (!OperatingSystem.current().isLinux) return Unavailable

            val cgroupDir = File("/sys/fs/cgroup")

            return if (cgroupDir.exists()) {
                Available(
                    memory = Memory(
                        memoryMax = File(cgroupDir, "memory.max"),
                        memoryCurrent = File(cgroupDir, "memory.current")
                    )
                )
            } else {
                Unavailable
            }
        }
    }
}
