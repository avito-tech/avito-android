package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.Result
import com.avito.android.runner.devices.internal.AndroidDebugBridge
import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.android.runner.devices.internal.RemoteDevice
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.Serial

internal interface RemoteDeviceProvider {
    suspend fun create(pod: KubePod): Result<RemoteDevice>
    suspend fun get(pod: KubePod): Result<RemoteDevice>
}

internal class RemoteDeviceProviderImpl(
    private val kubernetesApi: KubernetesApi,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val androidDebugBridge: AndroidDebugBridge,
    loggerFactory: LoggerFactory
) : RemoteDeviceProvider {

    private val logger: Logger = loggerFactory.create<RemoteDeviceProvider>()

    override suspend fun create(pod: KubePod): Result<RemoteDevice> {
        with(pod) {
            val podName = name
            logger.debug("Found new pod: $podName")
            return bootDevice().onFailure { error ->
                logger.warn(initializeFailureMessage(podName, ip), error)
                val isDeleted = kubernetesApi.deletePod(podName)
                logger.debug("Pod $podName is deleted: $isDeleted")
            }
        }
    }

    override suspend fun get(pod: KubePod): Result<RemoteDevice> {
        return pod.getDevice()
    }

    private fun initializeFailureMessage(podName: String, podIp: String?): String {
        return buildString {
            append("Pod $podName can't load device. Disconnect and delete.")
            if (!podIp.isNullOrBlank()) {
                appendLine()
                append("Check device logs in artifacts: ${emulatorsLogsReporter.getLogFile(podIp)}")
            }
        }
    }

    private suspend fun KubePod.bootDevice(): Result<RemoteDevice> {
        return getDevice()
            .flatMap { device ->
                device.waitForBoot()
                    .map { device }
                    .onFailure { device.disconnect() }
            }
    }

    private fun KubePod.getDevice(): Result<RemoteDevice> {
        return Result.tryCatch {
            val podIp = ip
            requireNotNull(podIp) { "Pod: $name must have an IP" }

            val name = portForward?.let { portForward ->
                "0.0.0.0:${portForward.localPort}"
            } ?: "$podIp:$ADB_DEFAULT_PORT"

            val serial = emulatorSerialName(name = name)

            androidDebugBridge.getRemoteDevice(
                serial = serial
            )
        }
    }

    private fun emulatorSerialName(name: String): Serial.Remote = Serial.Remote(name)
}

private const val ADB_DEFAULT_PORT = 5555
