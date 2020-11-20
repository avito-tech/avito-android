package com.avito.ci

import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.api.model.apps.DoneableDeployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.RollableScalableResource
import org.jetbrains.teamcity.rest.BuildState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private typealias Deployments = MixedOperation<Deployment,
    DeploymentList,
    DoneableDeployment,
    RollableScalableResource<Deployment, DoneableDeployment>>

class ClearK8SDeploymentsByNamespaces(
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
                    val now = Date(System.currentTimeMillis())
                    val hourInMillis = TimeUnit.HOURS.toMillis(1)
                    val hourAgo = Date(
                        now.toInstant()
                            .minusMillis(hourInMillis)
                            .toEpochMilli()
                    )
                    creationTime.before(hourAgo)
                }

                is DeploymentEnvironment.Service ->
                    false

                is DeploymentEnvironment.Unknown ->
                    throw IllegalStateException("Unknown environment in deployment:$description")
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
                deployments
                    .list()
                    .items
                    .forEach { deployment ->
                        deployments.checkDeploymentLeak(deployment)
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

    private fun Deployments.checkDeploymentLeak(deployment: Deployment) {
        try {
            if (deployment.isLeaked) {
                delete(deployment)
            }
        } catch (e: Throwable) {
            throw RuntimeException("Error when checked deployment=${deployment.description} leak", e)
        }
    }
}
