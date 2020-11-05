package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.client.kubernetes.ReservationDeploymentFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import java.io.File

interface DevicesProviderFactory {
    fun create(
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters
    ): DevicesProvider

    class Impl(
        private val kubernetesCredentials: KubernetesCredentials,
        private val buildId: String,
        private val buildType: String,
        private val projectName: String,
        private val registry: String,
        private val output: File,
        private val logcatDir: File,
        private val logger: CILogger
    ) : DevicesProviderFactory {
        override fun create(
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters
        ): DevicesProvider {
            val adb = Adb()
            val androidDebugBridge = AndroidDebugBridge(
                adb = adb,
                logger = { logger.info(it) }
            )
            val emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = output,
                logcatTags = executionParameters.logcatTags,
                logcatDir = logcatDir
            )
            val devicesManager = AdbDevicesManager(adb = adb, logger = commonLogger(logger))
            return when {
                configuration.isMockEmulator -> {
                    MockDevicesProvider(logger)
                }
                configuration.isTargetLocalEmulators -> {
                    LocalDevicesProvider(
                        androidDebugBridge = androidDebugBridge,
                        devicesManager = devicesManager,
                        emulatorsLogsReporter = emulatorsLogsReporter,
                        adb = adb,
                        logger = logger
                    )
                }
                else -> {
                    KubernetesDevicesProvider(
                        client = KubernetesReservationClient(
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
                        ),
                        adbDevicesManager = devicesManager,
                        adb = adb,
                        logger = logger
                    )
                }
            }
        }
    }
}
