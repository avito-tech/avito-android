package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.FakeAndroidDebugBridge
import com.avito.android.runner.devices.internal.StubEmulatorsLogsReporter
import com.avito.kotlin.dsl.getSystemProperty
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import java.io.File

internal fun KubernetesReservationClient.Companion.createStubInstance(
    loggerFactory: LoggerFactory,
    buildId: String = getSystemProperty(name = "teamcityBuildId", defaultValue = "local"),
    deploymentNameGenerator: DeploymentNameGenerator = StubDeploymentNameGenerator(),
    kubernetesUrl: String = getSystemProperty("kubernetesUrl"),
    kubernetesNamespace: String = getSystemProperty("kubernetesNamespace"),
    configurationName: String = "integration-test",
    projectName: String = "",
    buildType: String = "integration-test"
): KubernetesReservationClient {
    val kubernetesCredentials = KubernetesCredentials.Service(
        token = getSystemProperty("kubernetesToken"),
        caCertData = getSystemProperty("kubernetesCaCertData"),
        url = kubernetesUrl
    )

    return KubernetesReservationClient(
        androidDebugBridge = FakeAndroidDebugBridge(),
        kubernetesApi = KubernetesApi.Impl(
            kubernetesClient = createKubernetesClient(
                kubernetesCredentials = kubernetesCredentials,
                namespace = kubernetesNamespace
            ),
            loggerFactory = loggerFactory
        ),
        emulatorsLogsReporter = StubEmulatorsLogsReporter,
        loggerFactory = loggerFactory,
        reservationDeploymentFactory = ReservationDeploymentFactoryImpl(
            configurationName = configurationName,
            projectName = projectName,
            buildId = buildId,
            buildType = buildType,
            deploymentNameGenerator = deploymentNameGenerator,
            loggerFactory = loggerFactory
        )
    )
}
