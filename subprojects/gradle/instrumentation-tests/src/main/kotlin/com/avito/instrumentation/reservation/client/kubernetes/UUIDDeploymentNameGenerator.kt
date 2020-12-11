package com.avito.instrumentation.reservation.client.kubernetes

import java.util.UUID

class UUIDDeploymentNameGenerator : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-${UUID.randomUUID()}".kubernetesName()
}
