package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.AndroidDebugBridge
import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.android.runner.devices.internal.KubernetesDevicesProvider
import com.avito.android.runner.devices.internal.LocalDevicesProvider
import com.avito.android.runner.devices.internal.StubDevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactory
import com.avito.android.runner.devices.internal.kubernetes.UUIDDeploymentNameGenerator
import com.avito.android.runner.devices.model.DeviceType
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import java.io.File

public class DeviceProviderFactoryImpl(
    private val kubernetesCredentials: KubernetesCredentials,
    private val buildId: String,
    private val buildType: String,
    private val projectName: String,
    private val registry: String,
    private val output: File,
    private val logcatDir: File,
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val metricsConfig: RunnerMetricsConfig
) : DevicesProviderFactory {

    override fun create(
        deviceType: DeviceType,
        configurationName: String,
        logcatTags: Collection<String>,
        kubernetesNamespace: String
    ): DevicesProvider {
        val adb = Adb()
        val androidDebugBridge = AndroidDebugBridge(
            adb = adb,
            loggerFactory = loggerFactory
        )
        val emulatorsLogsReporter = EmulatorsLogsReporter(
            outputFolder = output,
            logcatTags = logcatTags,
            logcatDir = logcatDir
        )
        val devicesManager = AdbDevicesManager(
            adb = adb,
            loggerFactory = loggerFactory
        )
        return when (deviceType) {
            DeviceType.MOCK ->
                StubDevicesProvider(loggerFactory)

            DeviceType.LOCAL ->
                LocalDevicesProvider(
                    androidDebugBridge = androidDebugBridge,
                    devicesManager = devicesManager,
                    emulatorsLogsReporter = emulatorsLogsReporter,
                    adb = adb,
                    loggerFactory = loggerFactory,
                    timeProvider = timeProvider
                )

            DeviceType.CLOUD ->
                KubernetesDevicesProvider(
                    client = KubernetesReservationClient(
                        androidDebugBridge = androidDebugBridge,
                        kubernetesClient = createKubernetesClient(
                            kubernetesCredentials = kubernetesCredentials,
                            namespace = kubernetesNamespace
                        ),
                        reservationDeploymentFactory = ReservationDeploymentFactory(
                            configurationName = configurationName,
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
                    timeProvider = timeProvider,
                    metricsConfig = metricsConfig
                )
        }
    }
}
