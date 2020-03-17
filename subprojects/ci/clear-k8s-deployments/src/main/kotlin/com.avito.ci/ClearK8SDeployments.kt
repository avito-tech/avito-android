package com.avito.ci

import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.delimiter
import kotlinx.cli.required

object ClearK8SDeployments {
    private val parser = ArgParser("clear-k8s-deployments")

    private val teamcityUrl: String by parser
        .option(type = ArgType.String)
        .required()

    private val teamcityApiUser: String by parser
        .option(type = ArgType.String)
        .required()

    private val teamcityApiPassword: String by parser
        .option(type = ArgType.String)
        .required()

    private val kubernetesToken by parser
        .option(ArgType.String)
        .required()

    private val kubernetesUrl by parser
        .option(ArgType.String)
        .required()

    private val kubernetesCaCert by parser
        .option(ArgType.String)
        .required()

    private val namespaces by parser
        .option(
            type = ArgType.String,
            description = "Kubernetes namespaces where need clear"
        )
        .delimiter(",")
        .required()

    @JvmStatic
    fun main(args: Array<String>) {
        parser.parse(args)
        println("namespaces: $namespaces")
        val teamcity = TeamcityApi.Impl(
            url = teamcityUrl,
            user = teamcityApiUser,
            password = teamcityApiPassword
        )
        val kubernetesClient = DefaultKubernetesClient(
            ConfigBuilder()
                .withOauthToken(kubernetesToken)
                .withMasterUrl(kubernetesUrl)
                .withCaCertData(kubernetesCaCert)
                .build()
        )

        namespaces.forEach { namespace ->
            kubernetesClient.inNamespace(namespace)
                .apps()
                .deployments()
                .list()
                .items
                .forEach { deployment ->
                    println(deployment.metadata.name)
                    val labels = deployment.metadata.labels
                    val teamcityId = if (labels["type"]?.startsWith("teamcity") ?: false) {
                        requireNotNull(labels["id"]) {
                            "labels id and type must be together"
                        }
                    } else {
                        // todo remove after 2020.3.3 release
                        deployment.metadata.name.split('-')[0]
                    }
                    try {
                        val build = teamcity.getBuild(teamcityId)
                        println(build.state)
                    } catch (e: Throwable) {
                        // todo slack
                        println(e)
                    }
                }
        }
    }
}