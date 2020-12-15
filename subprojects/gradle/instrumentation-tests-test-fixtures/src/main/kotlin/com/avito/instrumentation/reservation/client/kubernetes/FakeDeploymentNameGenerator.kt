package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.utils.gradle.toValidKubernetesName

class FakeDeploymentNameGenerator(
    private val buildId: String
) : DeploymentNameGenerator {

    override fun generateName(namespace: String): String =
        "$namespace-itest-$buildId".toValidKubernetesName()
}
