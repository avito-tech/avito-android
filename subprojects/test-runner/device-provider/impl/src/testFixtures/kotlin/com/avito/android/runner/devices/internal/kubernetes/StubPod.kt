package com.avito.android.runner.devices.internal.kubernetes

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod

internal fun KubePod.Companion.createStubInstance(
    name: String = "stub-pod-name",
    phase: String = "Running",
    ip: String? = "stub-pod-ip"
): KubePod = KubePod(
    pod = Pod().apply {
        metadata {
            this.name = name
        }
        status {
            this.phase = phase
            podIP = ip
        }
    }
)
