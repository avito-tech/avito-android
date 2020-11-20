package com.avito.ci

import com.avito.teamcity.TeamcityApi
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.delimiter
import kotlinx.cli.required

object ClearK8SDeploymentsMain {
    @OptIn(ExperimentalCli::class)
    abstract class ClearDeployments(
        name: String,
        description: String
    ) : Subcommand(name, description) {
        protected val teamcityUrl: String by option(type = ArgType.String)
            .required()

        protected val teamcityApiUser: String by option(type = ArgType.String)
            .required()

        protected val teamcityApiPassword: String by option(type = ArgType.String)
            .required()

        protected val kubernetesToken by option(ArgType.String)
            .required()

        protected val kubernetesUrl by option(ArgType.String)
            .required()

        protected val kubernetesCaCert by option(ArgType.String)
            .required()
    }

    private val parser = ArgParser("clear-k8s-deployments")

    @ExperimentalCli
    @JvmStatic
    fun main(args: Array<String>) {
        class ByNamespaces :
            ClearDeployments("clearByNamespaces", "Clear given namespaces from leaked deployments") {

            private val namespaces by option(
                type = ArgType.String,
                description = "Kubernetes namespaces where need clear"
            ).delimiter(",").required()

            override fun execute() {
                ClearK8SDeploymentsByNamespaces(
                    teamcity = TeamcityApi.Impl(
                        url = teamcityUrl,
                        user = teamcityApiUser,
                        password = teamcityApiPassword
                    ),
                    kubernetesClient = DefaultKubernetesClient(
                        ConfigBuilder()
                            .withOauthToken(kubernetesToken)
                            .withMasterUrl(kubernetesUrl)
                            .withCaCertData(kubernetesCaCert)
                            .build()
                    )
                ).clear(namespaces)
            }
        }

        class ByNames :
            ClearDeployments("deleteByNames", "Delete deployments by names in given namespace") {

            private val namespace by option(
                type = ArgType.String,
                description = "Kubernetes namespace where will be deleted deployments"
            ).required()

            private val deploymentNames by option(
                type = ArgType.String,
                description = "Kubernetes deployment names that will be deleted"
            ).delimiter(",").required()

            override fun execute() {
                DeleteK8SDeploymentsByNames(
                    kubernetesClient = DefaultKubernetesClient(
                        ConfigBuilder()
                            .withOauthToken(kubernetesToken)
                            .withMasterUrl(kubernetesUrl)
                            .withCaCertData(kubernetesCaCert)
                            .build()
                    )
                ).delete(namespace, deploymentNames)
            }
        }
        parser.subcommands(ByNamespaces(), ByNames())
        parser.parse(args)
    }
}
