package com.avito.k8s

import com.avito.k8s.model.KubePod

public fun Collection<KubePod>.describePods(deploymentName: String): String {
    val pods = this
    return buildString {
        appendLine("Getting pods for deployment: $deploymentName:")
        appendLine("----------------")

        pods.forEach { pod -> appendLine(pod.toString()) }

        val runningCount = pods.count { it.phase == KubePod.PodPhase.Running }
        val pendingCount = pods.count { it.phase is KubePod.PodPhase.Pending }
        val otherCount = pods.size - runningCount - pendingCount

        append(
            "------- Summary: " +
                "running: $runningCount; " +
                "pending: $pendingCount; " +
                "other $otherCount  ---------"
        )
    }
}
