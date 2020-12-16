package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.kotlin.dsl.getSystemProperty
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.CILogger
import java.io.File

fun KubernetesReservationClient.Companion.createStubInstance(
    logger: CILogger,
    adb: Adb = Adb(),
    buildId: String = getSystemProperty(name = "teamcityBuildId", defaultValue = "local"),
    deploymentNameGenerator: DeploymentNameGenerator = FakeDeploymentNameGenerator(buildId),
    kubernetesUrl: String = getSystemProperty("avito.kubernetes.url"),
    kubernetesNamespace: String = getSystemProperty("avito.kubernetes.namespace"),
    configurationName: String = "integration-test",
    projectName: String = "",
    buildType: String = "integration-test", // see DeploymentEnvironment
    registry: String = ""
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
            logger = { logger.info(it) }
        ),
        kubernetesClient = createKubernetesClient(
            kubernetesCredentials = kubernetesCredentials,
            namespace = kubernetesNamespace
        ),
        emulatorsLogsReporter = EmulatorsLogsReporter(
            outputFolder = outputFolder,
            logcatDir = logcatFolder,
            logcatTags = emptyList()
        ),
        logger = logger,
        reservationDeploymentFactory = ReservationDeploymentFactory(
            configurationName = configurationName,
            projectName = projectName,
            buildId = buildId,
            buildType = buildType,
            registry = registry,
            deploymentNameGenerator = deploymentNameGenerator,
            logger = logger
        )
    )
}
