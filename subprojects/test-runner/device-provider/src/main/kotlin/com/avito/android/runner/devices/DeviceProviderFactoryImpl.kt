package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.AndroidDebugBridgeImpl
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterImpl
import com.avito.android.runner.devices.internal.KubernetesDevicesProvider
import com.avito.android.runner.devices.internal.LocalDevicesProvider
import com.avito.android.runner.devices.internal.StubDevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesApi
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactoryImpl
import com.avito.android.runner.devices.internal.kubernetes.UUIDDeploymentNameGenerator
import com.avito.android.runner.devices.model.DeviceType
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
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
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val statsDConfig: StatsDConfig,
    private val deviceType: DeviceType,
    private val projectName: String,
    private val configurationName: String,
    private val outputDir: File,
    private val logcatTags: Collection<String>,
    private val kubernetesNamespace: String,
    private val runnerPrefix: SeriesName,
) : DevicesProviderFactory {

    override fun create(tempLogcatDir: File): DevicesProvider {
        val adb = Adb()
        val androidDebugBridge = AndroidDebugBridgeImpl(
            adb = adb,
            loggerFactory = loggerFactory
        )
        val emulatorsLogsReporter = EmulatorsLogsReporterImpl(
            outputFolder = outputDir,
            logcatTags = logcatTags,
            logcatDir = tempLogcatDir
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
                        kubernetesApi = KubernetesApi.Impl(
                            kubernetesClient = createKubernetesClient(
                                httpClientProvider = HttpClientProvider(
                                    statsDSender = StatsDSender.Impl(statsDConfig, loggerFactory),
                                    timeProvider = timeProvider,
                                    loggerFactory = loggerFactory,
                                ),
                                kubernetesCredentials = kubernetesCredentials,
                                namespace = kubernetesNamespace
                            ),
                            loggerFactory = loggerFactory
                        ),
                        reservationDeploymentFactory = ReservationDeploymentFactoryImpl(
                            configurationName = configurationName,
                            projectName = projectName,
                            buildId = buildId,
                            buildType = buildType,
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
                    metricsConfig = RunnerMetricsConfig(statsDConfig, runnerPrefix)
                )
        }
    }
}
