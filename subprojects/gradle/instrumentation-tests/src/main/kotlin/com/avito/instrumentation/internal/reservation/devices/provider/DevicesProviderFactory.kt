package com.avito.instrumentation.internal.reservation.devices.provider

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.internal.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.internal.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.internal.reservation.client.kubernetes.ReservationDeploymentFactory
import com.avito.instrumentation.internal.reservation.client.kubernetes.UUIDDeploymentNameGenerator
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import java.io.File

internal interface DevicesProviderFactory {

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
        private val loggerFactory: LoggerFactory,
        private val timeProvider: TimeProvider
    ) : DevicesProviderFactory {

        override fun create(
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters
        ): DevicesProvider {
            val adb = Adb()
            val androidDebugBridge = AndroidDebugBridge(
                adb = adb,
                loggerFactory = loggerFactory
            )
            val emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = output,
                logcatTags = executionParameters.logcatTags,
                logcatDir = logcatDir
            )
            val devicesManager = AdbDevicesManager(
                adb = adb,
                loggerFactory = loggerFactory
            )
            return when (configuration.requestedDeviceType) {
                InstrumentationConfiguration.Data.DevicesType.MOCK ->
                    StubDevicesProvider(loggerFactory)

                InstrumentationConfiguration.Data.DevicesType.LOCAL ->
                    LocalDevicesProvider(
                        androidDebugBridge = androidDebugBridge,
                        devicesManager = devicesManager,
                        emulatorsLogsReporter = emulatorsLogsReporter,
                        adb = adb,
                        loggerFactory = loggerFactory,
                        timeProvider = timeProvider
                    )

                InstrumentationConfiguration.Data.DevicesType.CLOUD ->
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
                                deploymentNameGenerator = UUIDDeploymentNameGenerator(),
                                loggerFactory = loggerFactory
                            ),
                            loggerFactory = loggerFactory,
                            emulatorsLogsReporter = emulatorsLogsReporter
                        ),
                        adbDevicesManager = devicesManager,
                        adb = adb,
                        loggerFactory = loggerFactory,
                        timeProvider = timeProvider
                    )
            }
        }
    }
}
