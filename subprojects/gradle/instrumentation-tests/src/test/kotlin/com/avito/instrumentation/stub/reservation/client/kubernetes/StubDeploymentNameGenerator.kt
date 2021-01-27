package com.avito.instrumentation.stub.reservation.client.kubernetes

import com.avito.instrumentation.internal.reservation.client.kubernetes.DeploymentNameGenerator
import com.avito.utils.gradle.toValidKubernetesName
import java.util.UUID

/**
 * @param postfix ability to pass stable postfix of deployment name for testing
 */
class StubDeploymentNameGenerator(
    private val postfix: String = UUID.randomUUID().toString()
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-$postfix".toValidKubernetesName()
}
