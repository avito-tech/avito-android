package com.avito.ci

import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.jetbrains.teamcity.rest.BuildState
import java.lang.IllegalStateException
import java.util.Date
import java.util.concurrent.TimeUnit

class ClearK8SDeploymentsByNamespaces(
    private val teamcity: TeamcityApi,
    private val kubernetesClient: DefaultKubernetesClient
) {
    fun clear(namespaces: List<String>) {
        namespaces.forEach { namespace ->
            println("Processing namespace=$namespace")
            try {
                val deployments = kubernetesClient.inNamespace(namespace)
                    .apps()
                    .deployments()
                deployments
                    .list()
                    .items
                    .forEach { deployment ->
                        println("Processing deployment=${deployment.description}")
                        try {
                            if (deployment.isLeaked) {
                                deployments.delete(deployment)
                            }
                        } catch (e: Throwable) {
                            // todo slack/sentry notification
                            println("Error with ${deployment.description}, $e")
                            e.printStackTrace()
                        }
                    }
            } catch (e: Throwable) {
                println("Error with $namespace, $e")
                e.printStackTrace()
            }
        }
    }

    private val Deployment.description: String
        get() = "[name=${metadata.name}, labels=${metadata.labels}]"

    private val Deployment.isLeaked: Boolean
        get() {
            return when (val env = environment) {
                is DeploymentEnvironment.Teamcity -> {
                    when (teamcity.getBuild(env.buildId).state) {
                        BuildState.FINISHED,
                        BuildState.DELETED,
                        BuildState.UNKNOWN -> true
                        BuildState.QUEUED,
                        BuildState.RUNNING -> false
                    }
                }
                is DeploymentEnvironment.Local -> {
                    val creation = Date(env.creationTimeInMillis)
                    val now = Date(System.currentTimeMillis())
                    val hourAgo = Date(
                        now.toInstant()
                            .minusMillis(TimeUnit.HOURS.toMillis(1))
                            .toEpochMilli()
                    )
                    creation.before(hourAgo)
                }
                is DeploymentEnvironment.Unknown -> throw IllegalStateException("Unknown environment in deployment:$description")
            }
        }
}