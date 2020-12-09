package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.utils.logging.CILogger
import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.newDeployment
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newEnvVar
import com.fkorotkov.kubernetes.newHostPathVolumeSource
import com.fkorotkov.kubernetes.newToleration
import com.fkorotkov.kubernetes.newVolume
import com.fkorotkov.kubernetes.newVolumeMount
import com.fkorotkov.kubernetes.resources
import com.fkorotkov.kubernetes.securityContext
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import java.util.UUID

class ReservationDeploymentFactory(
    private val configurationName: String,
    private val projectName: String,
    private val buildId: String,
    private val buildType: String,
    private val registry: String,
    logger: CILogger
) {

    private val logger = logger.child("ReservationDeploymentFactory")

    init {
        val prefix = { reason: String -> "Can't create configuration, precondition failed: $reason" }
        require(configurationName.isNotBlank()) { prefix.invoke("configurationName is blank; used to label pods") }
        require(buildId.isNotBlank()) { prefix.invoke("buildId is blank, client can't distinguish reservations") }
    }

    fun createDeployment(namespace: String, reservation: Reservation.Data): Deployment {
        logger.debug("Creating deployment for configuration: $configurationName")
        val deploymentName = generateDeploymentName(namespace)
        logger.debug("Deployment name will be: $deploymentName")
        return when (reservation.device) {
            is Device.LocalEmulator -> throw IllegalStateException(
                "Local emulator ${reservation.device} is unsupported in kubernetes reservation"
            )
            is Device.Phone -> getDeviceDeployment(
                count = reservation.count,
                phone = reservation.device,
                deploymentName = deploymentName
            )
            is Device.CloudEmulator -> getCloudEmulatorDeployment(
                emulator = reservation.device,
                deploymentName = deploymentName,
                count = reservation.count
            )
            is Device.MockEmulator -> throw IllegalStateException(
                "Mock emulator ${reservation.device} is unsupported in kubernetes reservation"
            )
        }
    }

    private fun getDeviceDeployment(
        count: Int,
        phone: Device.Phone,
        deploymentName: String,
        kubernetesNodeName: String = "avi-training06" // temporary node, remove later
    ): Deployment {
        return deviceDeployment(
            deploymentMatchLabels = deviceMatchLabels(phone),
            deploymentName = deploymentName,
            count = count
        ) {
            containers = listOf(
                newContainer {
                    name = phone.name.kubernetesName()
                    image = "$registry/${phone.proxyImage}"

                    securityContext {
                        privileged = true
                    }
                    resources {
                        limits = mapOf(
                            "android/device" to Quantity("1")
                        )
                        requests = mapOf(
                            "android/device" to Quantity("1")
                        )
                    }
                }
            )
            dnsPolicy = "ClusterFirst"
            nodeName = kubernetesNodeName
            tolerations = listOf(
                newToleration {
                    operator = "Exists"
                    effect = "NoSchedule"
                }
            )
        }
    }

    private fun getCloudEmulatorDeployment(
        emulator: Device.CloudEmulator,
        deploymentName: String,
        count: Int
    ): Deployment {
        return deviceDeployment(
            deploymentMatchLabels = deviceMatchLabels(emulator),
            deploymentName = deploymentName,
            count = count
        ) {
            containers = listOf(
                newContainer {
                    name = emulator.name.kubernetesName()
                    image = "$registry/${emulator.image}"

                    securityContext {
                        privileged = true
                    }

                    resources {
                        limits = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresLimit),
                            "memory" to Quantity(emulator.memoryLimit)
                        )
                        requests = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresRequest)
                        )
                    }

                    if (emulator.gpu) {
                        volumeMounts = listOf(
                            newVolumeMount {
                                name = "x-11"
                                mountPath = "/tmp/.X11-unix"
                                readOnly = true
                            }
                        )

                        env = listOf(
                            newEnvVar {
                                name = "GPU_ENABLED"
                                value = "true"
                            },
                            newEnvVar {
                                name = "SNAPSHOT_DISABLED"
                                value = "true"
                            }
                        )
                    }
                }
            )

            if (emulator.gpu) {
                volumes = listOf(
                    newVolume {
                        name = "x-11"

                        hostPath = newHostPathVolumeSource {
                            path = "/tmp/.X11-unix"
                            type = "Directory"
                        }
                    }
                )
            }

            tolerations = listOf(
                newToleration {
                    key = "dedicated"
                    operator = "Equal"
                    value = "android"
                    effect = "NoSchedule"
                }
            )
        }
    }

    private fun deviceDeployment(
        deploymentMatchLabels: Map<String, String>,
        deploymentName: String,
        count: Int,
        block: PodSpec.() -> Unit
    ): Deployment {
        val deploymentSpecificationsMatchLabels = deploymentMatchLabels
            .plus("deploymentName" to deploymentName)

        return newDeployment {
            apiVersion = "extensions/v1beta1"
            metadata {
                name = deploymentName
                labels = deploymentMatchLabels
                finalizers = listOf(
                    // Remove all dependencies (replicas) in foreground after removing deployment
                    "foregroundDeletion"
                )
            }
            spec {
                replicas = count
                selector {
                    matchLabels = deploymentSpecificationsMatchLabels
                }

                template {
                    metadata {
                        labels = deploymentSpecificationsMatchLabels
                    }
                    spec(block)
                }
            }
        }
    }

    private fun deviceMatchLabels(
        device: Device
    ): Map<String, String> {
        return mapOf(
            "type" to buildType, // teamcity or local
            "id" to buildId, // teamcity_build_id or local synthetic
            "project" to projectName,
            "instrumentationConfiguration" to configurationName,
            "device" to device.description
        )
    }

    private fun generateDeploymentName(namespace: String): String = "$namespace-${UUID.randomUUID()}".kubernetesName()

    private fun String.kubernetesName(): String = replace("_", "-").toLowerCase()
}
