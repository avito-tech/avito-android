package com.avito.android.runner.devices.internal.kubernetes

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod

internal fun StubPod(
    name: String = "stub-pod-name",
    phase: String = KubernetesApi.POD_STATUS_RUNNING,
    ip: String? = "stub-pod-ip"
): Pod = Pod().apply {
    metadata {
        this.name = name
    }
    status {
        this.phase = phase
        podIP = ip
    }
}
