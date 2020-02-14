package com.avito.android.plugin

import com.avito.android.docker.Docker
import com.avito.android.sentry.sentry
import com.avito.utils.buildFailer
import com.avito.utils.gradle.kubernetesCredentials
import com.fkorotkov.kubernetes.backend
import com.fkorotkov.kubernetes.http
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newContainerPort
import com.fkorotkov.kubernetes.newDeployment
import com.fkorotkov.kubernetes.newHTTPIngressPath
import com.fkorotkov.kubernetes.newIngress
import com.fkorotkov.kubernetes.newIngressRule
import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.newServicePort
import com.fkorotkov.kubernetes.newToleration
import com.fkorotkov.kubernetes.resources
import com.fkorotkov.kubernetes.selector
import com.fkorotkov.kubernetes.servicePort
import com.fkorotkov.kubernetes.spec
import com.fkorotkov.kubernetes.targetPort
import com.fkorotkov.kubernetes.template
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.extensions.Ingress
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class DeployDocsTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputDirectory
    val docsDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Input
    val registry = objects.property<String>()

    @get:Input
    val targetHost = objects.property<String>()

    @TaskAction
    fun doWork() {
        val docker = Docker.fromProject(project)

        docker.build(docsDirectory.asFile.get())
            .flatMap { imageId ->
                logger.lifecycle("Image successfully built: $imageId")
                val imageName = Docker.ImageName(
                    name = "android/docs",
                    tag = imageId.take(10),
                    registry = registry.get()
                )
                docker.tag(imageId, imageName)
            }
            .flatMap { imageName ->
                logger.lifecycle("Image successfully tagged: $imageName") // TODO: build with tags
                docker.push(imageName)
            }.fold(
                { imageName ->
                    logger.lifecycle("Image $imageName successfully pushed")
                    deployToKubernetes(imageName, targetHost.get())
                },
                { exception ->
                    project.sentry.orNull?.sendException(exception)
                        ?: project.buildFailer.failBuild(exception.message ?: "no message", exception)
                })
    }

    private fun deployToKubernetes(imageName: Docker.ImageName, targetHost: String) {
        val namespace = "android-emulator"
        val deploymentName = "android-docs"

        val kubernetesCredentials = project.kubernetesCredentials

        val kubernetesClient: KubernetesClient = DefaultKubernetesClient(
            ConfigBuilder()
                .withCaCertData(kubernetesCredentials.caCertData)
                .withMasterUrl(kubernetesCredentials.url)
                .withOauthToken(kubernetesCredentials.token)
                .build()
        ).inNamespace(namespace)

        kubernetesClient.run {
            apps().deployments().createOrReplace(
                createDocsDeployment(imageName, deploymentName, namespace)
            )
            extensions().ingresses().createOrReplace(
                createIngress(deploymentName, namespace, targetHost)
            )
            services().createOrReplace(
                createService(deploymentName, namespace)
            )
        }
    }

    private fun createIngress(
        deploymentName: String,
        space: String,
        targetHost: String
    ): Ingress = newIngress {
        apiVersion = "extensions/v1beta1"

        metadata {
            name = deploymentName
            namespace = space
            annotations = mapOf(
                "kubernetes.io/ingress.class" to "service",
                "ingress.kubernetes.io/proxy-body-size" to "200m",
                "nginx.ingress.kubernetes.io/proxy-body-size" to "200m",
                "nginx.org/client-max-body-size" to "200m"
            )
        }

        spec {
            rules = listOf(newIngressRule {
                host = targetHost
                http {
                    paths = listOf(newHTTPIngressPath {
                        path = "/"
                        backend {
                            serviceName = deploymentName
                            servicePort {
                                intVal = 80
                            }
                        }
                    })
                }
            })
        }
    }

    private fun createService(deploymentName: String, space: String) = newService {
        apiVersion = "v1"

        metadata {
            name = deploymentName
            namespace = space
            labels = mapOf(
                "name" to deploymentName,
                "service" to deploymentName
            )
        }

        spec {
            selector = mapOf("app" to deploymentName)
            ports = listOf(newServicePort {
                protocol = "TCP"
                port = 80
                targetPort {
                    intVal = 80
                }
            })
        }
    }

    private fun createDocsDeployment(imageName: Docker.ImageName, deploymentName: String, space: String): Deployment {

        val deploymentMatchLabels = mapOf(
            "app" to deploymentName,
            "service" to deploymentName
        )

        return newDeployment {
            apiVersion = "extensions/v1beta1"

            metadata {
                name = deploymentName
                namespace = space
                labels = deploymentMatchLabels
                finalizers = listOf(
                    // Remove all dependencies (replicas) in foreground after removing deployment
                    "foregroundDeletion"
                )
            }

            spec {
                replicas = 1
                //  the number of old ReplicaSets to retain to allow rollback
                revisionHistoryLimit = 1
                selector {
                    matchLabels = deploymentMatchLabels
                }
                template {
                    metadata {
                        labels = deploymentMatchLabels
                    }
                    spec {
                        containers = listOf(
                            newContainer {
                                name = deploymentName
                                image = imageName.toString()
                                imagePullPolicy = "IfNotPresent"

                                resources {
                                    limits = mapOf(
                                        "cpu" to Quantity("1"),
                                        "memory" to Quantity("1Gi")
                                    )
                                }

                                ports = listOf(newContainerPort {
                                    containerPort = 80
                                })
                            }
                        )

                        tolerations = listOf(newToleration {
                            key = "dedicated"
                            operator = "Equal"
                            value = "android"
                            effect = "NoSchedule"
                        })
                    }
                }
            }
        }
    }
}
