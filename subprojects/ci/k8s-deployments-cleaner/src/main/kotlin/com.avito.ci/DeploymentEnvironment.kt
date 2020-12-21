package com.avito.ci

import com.avito.ci.DeploymentEnvironment.IntegrationTest
import com.avito.ci.DeploymentEnvironment.Local
import com.avito.ci.DeploymentEnvironment.Service
import com.avito.ci.DeploymentEnvironment.Teamcity
import com.avito.ci.DeploymentEnvironment.Unknown
import io.fabric8.kubernetes.api.model.apps.Deployment

sealed class DeploymentEnvironment {

    data class Teamcity(val buildId: String) : DeploymentEnvironment()

    object Local : DeploymentEnvironment()

    /**
     * environment for long lived pods
     */
    object Service : DeploymentEnvironment()

    /**
     * Incorrectly finished integration tests
     */
    data class IntegrationTest(val buildId: String) : DeploymentEnvironment()

    object Unknown : DeploymentEnvironment()
}

val Deployment.environment: DeploymentEnvironment
    get() {
        val type = metadata.labels["type"]
        return when {
            type == null -> Unknown
            type.startsWith("service") -> Service
            type.startsWith("local") -> Local
            type.startsWith("teamcity") -> Teamcity(
                buildId = requireNotNull(metadata.labels["id"])
            )

            /**
             * see StubKubernetesReservationClient
             */
            type.startsWith("integration-test") -> IntegrationTest(
                buildId = requireNotNull(metadata.labels["id"])
            )

            else -> Unknown
        }
    }
