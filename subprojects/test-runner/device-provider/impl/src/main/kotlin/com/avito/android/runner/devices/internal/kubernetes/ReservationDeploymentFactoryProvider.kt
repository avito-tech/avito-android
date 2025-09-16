package com.avito.android.runner.devices.internal.kubernetes

import com.avito.logger.LoggerFactory
import com.avito.utils.gradle.KubernetesCredentials

public class ReservationDeploymentFactoryProvider(
    private val configurationName: String,
    private val projectName: String,
    private val buildId: String,
    private val buildType: String,
    private val loggerFactory: LoggerFactory,
    private val useLegacyExtensionsV1Beta: Boolean,
    private val kubernetesCredentials: KubernetesCredentials
) {
    internal fun provide(): ReservationDeploymentFactory {
        val tolerations = when (kubernetesCredentials) {
            is KubernetesCredentials.Service -> kubernetesCredentials.tolerations
            is KubernetesCredentials.Config -> kubernetesCredentials.tolerations
            is KubernetesCredentials.Empty -> emptyList()
        }

        return ReservationDeploymentFactoryImpl(
            configurationName = configurationName,
            projectName = projectName,
            buildId = buildId,
            buildType = buildType,
            deploymentNameGenerator = UUIDDeploymentNameGenerator(),
            loggerFactory = loggerFactory,
            useLegacyExtensionsV1Beta = useLegacyExtensionsV1Beta,
            tolerations = tolerations,
        )
    }
}
