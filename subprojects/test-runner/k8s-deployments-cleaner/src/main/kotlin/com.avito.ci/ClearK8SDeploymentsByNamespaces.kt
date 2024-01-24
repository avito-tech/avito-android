package com.avito.ci

import com.avito.ci.DeploymentEnvironment.IntegrationTest
import com.avito.ci.DeploymentEnvironment.Local
import com.avito.ci.DeploymentEnvironment.Service
import com.avito.ci.DeploymentEnvironment.Teamcity
import com.avito.ci.DeploymentEnvironment.Unknown
import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import org.jetbrains.teamcity.rest.BuildState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

internal class ClearK8SDeploymentsByNamespaces(
    private val teamcity: TeamcityApi,
    private val kubernetesClient: DefaultKubernetesClient
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Moscow")
    }

    private val Deployment.description: String
        get() = "[name=${metadata.name}, namespace=${metadata.namespace}, labels=${metadata.labels}]"

    private val Deployment.isLeaked: Boolean
        get() {
            return when (val env = environment) {

                is Teamcity -> noActiveTeamcityBuilds(env.buildId)

                is Local -> creationTime.before(minutesAgo(60))

                is Service -> false

                is IntegrationTest -> creationTime.before(minutesAgo(4)) || noActiveTeamcityBuilds(env.buildId)

                is Unknown -> throw IllegalStateException("Unknown environment in deployment:$description")
            }
        }

    private val Deployment.creationTime: Date
        get() = dateFormat.parse(metadata.creationTimestamp)

    fun clear(namespaces: List<String>) {
        namespaces.forEach { namespace ->
            try {
                val deployments = kubernetesClient.inNamespace(namespace)
                    .apps()
                    .deployments()

                val replicas = kubernetesClient.inNamespace(namespace)
                    .apps()
                    .replicaSets()

                val deploymentToReplicas = replicas
                    .list()
                    .items
                    .groupBy {
                        it.metadata.labels["deploymentName"]
                    }

                val pods = kubernetesClient.inNamespace(namespace)
                    .pods()

                val deploymentToPods = pods
                    .list()
                    .items
                    .groupBy {
                        it.metadata.labels["deploymentName"]
                    }

                deployments
                    .list()
                    .items
                    .forEach { deployment ->
                        try {
                            if (deployment.isLeaked) {
                                println("Found leaky ${deployment.description}")
                                val deleted = deleteResource(deployments, deployment.metadata.name)
                                println("${deployment.description} deleted is $deleted")

                                deploymentToReplicas[deployment.metadata.name]?.let { replicasToDelete ->
                                    val deletedCount = replicasToDelete
                                        .map { deleteResource(replicas, it.metadata.name) }
                                        .count { it }
                                    println(
                                        "$deletedCount of ${replicasToDelete.count()} replicas " +
                                            "of ${deployment.description} are deleted"
                                    )
                                }

                                deploymentToPods[deployment.metadata.name]?.let { podsToDelete ->
                                    val deletedCount = podsToDelete
                                        .map { deleteResource(pods, it.metadata.name) }
                                        .count { it }
                                    println(
                                        "$deletedCount of ${podsToDelete.count()} pods " +
                                            "of ${deployment.description} are deleted"
                                    )
                                }
                            } else {
                                println("Found not leaky ${deployment.description}")
                            }
                        } catch (e: Throwable) {
                            throw RuntimeException("Error when checked deployment=${deployment.description} leak", e)
                        }
                    }
            } catch (e: Throwable) {
                throw RuntimeException(
                    "Error when checked leak in namespace=$namespace. " +
                        "If reason wasn't clear read previous discussion http://links.k.avito.ru/Az",
                    e
                )
            }
        }
    }

    private fun <T, L, R : Resource<T>> deleteResource(
        resource: MixedOperation<T, L, R>,
        nameToDelete: String
    ): Boolean {
        return resource
            .withName(nameToDelete)
            .withGracePeriod(0)
            .delete()
    }

    private fun minutesAgo(minutes: Long): Date {
        val now = Date(System.currentTimeMillis())
        val minutesInMillis = TimeUnit.MINUTES.toMillis(minutes)
        return Date(
            now.toInstant()
                .minusMillis(minutesInMillis)
                .toEpochMilli()
        )
    }

    private fun noActiveTeamcityBuilds(buildId: String): Boolean {
        return if (buildId.startsWith("local")) {
            // see EnvArgs.Build.Local and StubKubernetesReservationClient
            true
        } else {
            when (teamcity.getBuild(buildId).state) {
                BuildState.FINISHED,
                BuildState.DELETED,
                BuildState.UNKNOWN -> true

                BuildState.QUEUED,
                BuildState.RUNNING -> false
            }
        }
    }
}
