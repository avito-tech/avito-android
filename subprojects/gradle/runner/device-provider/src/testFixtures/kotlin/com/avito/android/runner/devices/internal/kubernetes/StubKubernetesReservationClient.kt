package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.AndroidDebugBridge
import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.kotlin.dsl.getSystemProperty
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import java.io.File

internal fun KubernetesReservationClient.Companion.createStubInstance(
    loggerFactory: LoggerFactory,
    adb: Adb = Adb(),
    buildId: String = getSystemProperty(name = "teamcityBuildId", defaultValue = "local"),
    deploymentNameGenerator: DeploymentNameGenerator = StubDeploymentNameGenerator(buildId),
    kubernetesUrl: String = getSystemProperty("avito.kubernetes.url"),
    kubernetesNamespace: String = getSystemProperty("avito.kubernetes.namespace"),
    configurationName: String = "integration-test",
    projectName: String = "",
    buildType: String = "integration-test"
): KubernetesReservationClient {
    val kubernetesCredentials = KubernetesCredentials.Service(
        token = getSystemProperty("avito.kubernetes.token"),
        caCertData = getSystemProperty("avito.kubernetes.cert"),
        url = kubernetesUrl
    )

    val outputFolder = File("integration")
    val logcatFolder = File("logcat")

    return KubernetesReservationClient(
        androidDebugBridge = AndroidDebugBridge(
            adb = adb,
            loggerFactory = loggerFactory
        ),
        kubernetesApi = KubernetesApi.Impl(
            kubernetesClient = createKubernetesClient(
                kubernetesCredentials = kubernetesCredentials,
                namespace = kubernetesNamespace
            ),
            loggerFactory = loggerFactory
        ),
        emulatorsLogsReporter = EmulatorsLogsReporter(
            outputFolder = outputFolder,
            logcatDir = logcatFolder,
            logcatTags = emptyList()
        ),
        loggerFactory = loggerFactory,
        reservationDeploymentFactory = ReservationDeploymentFactory(
            configurationName = configurationName,
            projectName = projectName,
            buildId = buildId,
            buildType = buildType,
            deploymentNameGenerator = deploymentNameGenerator,
            loggerFactory = loggerFactory
        )
    )
}
