package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.test.gradle.getTestProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.CILogger
import java.io.File

fun KubernetesReservationClient.Companion.createStubInstance(
    logger: CILogger,
    deploymentNameGenerator: DeploymentNameGenerator = FakeDeploymentNameGenerator(),
    kubernetesUrl: String = getTestProperty("avito.kubernetes.url"),
    kubernetesNamespace: String = getTestProperty("avito.kubernetes.namespace"),
    configurationName: String = "integration-test",
    projectName: String = "",
    buildId: String = "1",
    buildType: String = "",
    registry: String = ""
): KubernetesReservationClient {
    val kubernetesCredentials = KubernetesCredentials.Service(
        token = getTestProperty("avito.kubernetes.token"),
        caCertData = getTestProperty("avito.kubernetes.cert"),
        url = kubernetesUrl
    )

    val outputFolder = File("integration")
    val logcatFolder = File("logcat")

    return KubernetesReservationClient(
        androidDebugBridge = AndroidDebugBridge(
            adb = Adb(),
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
