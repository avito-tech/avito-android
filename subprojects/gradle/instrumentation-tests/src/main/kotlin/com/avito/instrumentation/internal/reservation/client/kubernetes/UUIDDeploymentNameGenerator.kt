package com.avito.instrumentation.internal.reservation.client.kubernetes

import com.avito.utils.gradle.toValidKubernetesName
import java.util.UUID

internal class UUIDDeploymentNameGenerator : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-${UUID.randomUUID()}".toValidKubernetesName()
}
