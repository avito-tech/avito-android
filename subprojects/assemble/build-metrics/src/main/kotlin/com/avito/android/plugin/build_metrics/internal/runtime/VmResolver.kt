package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.command.Jps
import org.gradle.process.internal.worker.GradleWorkerMain
import java.util.stream.Collectors

internal class VmResolver(
    private val jps: Jps
) {

    /**
     * Resolves Gradle processes with their descendants
     */
    fun localVMs(): Result<Set<LocalVm>> {
        val allVmsResult = jps.run()
        if (allVmsResult.isFailure()) {
            return Result.Failure((allVmsResult as Result.Failure).throwable)
        }
        val allVms = allVmsResult.getOrThrow()
            .map { it.recoverType() }
            .toSet()

        return Result.tryCatch {
            filterGradleRelatedVms(allVms)
        }
    }

    private fun filterGradleRelatedVms(allVms: Set<LocalVm>): Set<LocalVm> {
        val vms = mutableSetOf<LocalVm>()

        val gradleDaemons = allVms.filterIsInstance<GradleDaemon>().toSet()

        vms.addAll(gradleDaemons)
        vms.addAll(filterChildren(allVms, gradleDaemons))
        // Kotlin daemon lives longer than Gradle daemon.
        // In this case it will have PPID = 1.
        // This is why we can't find it as a child of Gradle daemon.
        vms.addAll(allVms.filterIsInstance<KotlinDaemon>())

        return vms
    }

    private fun filterChildren(allVms: Set<LocalVm>, parents: Set<GradleDaemon>): Set<LocalVm> {
        return parents.flatMap { vm ->
            val process = ProcessHandle.of(vm.id)
            if (process.isPresent) {
                val descendantsIds = process.get()
                    .descendants()
                    .map { it.pid() }
                    .collect(Collectors.toSet())

                allVms.filter { descendantsIds.contains(it.id) }
            } else {
                emptySet()
            }
        }.toSet()
    }

    private fun LocalVm.Unknown.recoverType(): LocalVm {
        return when {
            name == org.gradle.launcher.daemon.bootstrap.GradleDaemon::class.java.canonicalName -> GradleDaemon(id)
            name == "org.jetbrains.kotlin.daemon.KotlinCompileDaemon" -> KotlinDaemon(id)
            name.contains(GradleWorkerMain::class.java.canonicalName) -> GradleWorker(id)
            else -> this
        }
    }
}
