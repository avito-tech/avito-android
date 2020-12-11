package com.avito.instrumentation.reservation.client.kubernetes

class FakeDeploymentNameGenerator(
    private val variablePart: String = "integration-test"
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-$variablePart".kubernetesName()
}
