package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.client.kubernetes.ReservationDeploymentFactory
import com.avito.instrumentation.reservation.client.local.LocalReservationClient
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import java.io.File

interface ReservationClientFactory {
    fun create(
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters
    ): ReservationClient

    class Impl(
        private val logger: CILogger,
        private val buildId: String,
        private val buildType: String,
        private val projectName: String,
        private val kubernetesCredentials: KubernetesCredentials,
        private val registry: String,
        private val output: File,
        private val logcatDir: File
    ) : ReservationClientFactory {

        override fun create(
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters
        ): ReservationClient {
            val emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = output,
                logcatTags = executionParameters.logcatTags,
                logcatDir = logcatDir
            )
            val androidDebugBridge = AndroidDebugBridge(
                logger = { logger.info(it) }
            )
            return if (configuration.isTargetLocalEmulators) {
                LocalReservationClient(
                    androidDebugBridge = androidDebugBridge,
                    devicesManager = AdbDevicesManager(logger = commonLogger(logger)),
                    configurationName = configuration.name,
                    logger = logger,
                    emulatorsLogsReporter = emulatorsLogsReporter
                )
            } else {
                KubernetesReservationClient(
                    androidDebugBridge = androidDebugBridge,
                    kubernetesClient = createKubernetesClient(
                        kubernetesCredentials = kubernetesCredentials,
                        namespace = executionParameters.namespace
                    ),
                    reservationDeploymentFactory = ReservationDeploymentFactory(
                        configurationName = configuration.name,
                        projectName = projectName,
                        buildId = buildId,
                        buildType = buildType,
                        registry = registry,
                        logger = logger
                    ),
                    logger = logger,
                    emulatorsLogsReporter = emulatorsLogsReporter
                )
            }
        }
    }
}
