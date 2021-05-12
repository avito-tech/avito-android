package com.avito.android.runner.devices.internal.kubernetes

import com.avito.utils.gradle.toValidKubernetesName
import java.util.UUID

/**
 * @param postfix ability to pass stable postfix of deployment name for testing
 */
internal class StubDeploymentNameGenerator(
    private val postfix: String = UUID.randomUUID().toString()
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-$postfix".toValidKubernetesName()
}
