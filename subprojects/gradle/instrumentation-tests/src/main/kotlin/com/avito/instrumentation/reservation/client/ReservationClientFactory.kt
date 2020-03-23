package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.client.local.LocalReservationClient
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.runner.logging.Logger
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.File

interface ReservationClientFactory {
    fun create(
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters,
        testsToRun: List<TestWithTarget>
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
            executionParameters: ExecutionParameters,
            testsToRun: List<TestWithTarget>
        ): ReservationClient {
            val hasLocal = testsToRun.any { it.target.reservation.device is Device.LocalEmulator }
            val hasKubernetes = testsToRun.any { it.target.reservation.device is Device.Emulator }
            if (hasLocal && hasKubernetes) {
                throw IllegalStateException("Scheduling to local and kubernetes emulators at the same time is not supported yet")
            }
            val emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = output,
                logcatTags = executionParameters.logcatTags,
                logcatDir = logcatDir
            )
            val androidDebugBridge = AndroidDebugBridge(
                logger = { logger.info(it) }
            )
            return if (hasLocal) {
                LocalReservationClient(
                    androidDebugBridge = androidDebugBridge,
                    devicesManager = AdbDevicesManager(logger = object : Logger {
                        override fun notify(message: String, error: Throwable?) {
                            logger.critical(message, error)
                        }

                        override fun log(message: String) {
                            logger.info(message)
                        }
                    }),
                    configurationName = configuration.name,
                    logger = logger,
                    emulatorsLogsReporter = emulatorsLogsReporter
                )
            } else {
                KubernetesReservationClient(
                    androidDebugBridge = androidDebugBridge,
                    kubernetesClient = DefaultKubernetesClient(
                        ConfigBuilder()
                            .withCaCertData(kubernetesCredentials.caCertData)
                            .withMasterUrl(kubernetesCredentials.url)
                            .withOauthToken(kubernetesCredentials.token)
                            .build()
                    )
                        .inNamespace(executionParameters.namespace),
                    configurationName = configuration.name,
                    projectName = projectName,
                    logger = logger,
                    buildId = buildId,
                    buildType = buildType,
                    emulatorsLogsReporter = emulatorsLogsReporter,
                    registry = registry
                )
            }
        }
    }
}
