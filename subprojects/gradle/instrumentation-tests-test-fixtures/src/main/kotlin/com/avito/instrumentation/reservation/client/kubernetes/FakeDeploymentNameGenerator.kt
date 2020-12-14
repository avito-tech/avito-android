package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.utils.gradle.toValidKubernetesName

class FakeDeploymentNameGenerator(
    private val variablePart: String = "integration-test"
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-$variablePart".toValidKubernetesName()
}
