package com.avito.k8s.model

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod

public fun KubePod.Companion.createStubInstance(
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
    },
    portForward = kubernetesClient.pods().withName(it.metadata.name).portForward(5555)
)
