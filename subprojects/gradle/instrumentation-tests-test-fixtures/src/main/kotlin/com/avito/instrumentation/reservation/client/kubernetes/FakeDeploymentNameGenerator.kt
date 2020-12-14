package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.utils.gradle.toValidKubernetesName

/**
 * @param postfix stable postfix of deployment name for testing (production variant contains random sequence)
 *   see [UUIDDeploymentNameGenerator]
 */
class FakeDeploymentNameGenerator(
    private val postfix: String = "integration-test"
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-$postfix".toValidKubernetesName()
}
