package com.avito.plugin

import Slf4jGradleLoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.time.DefaultTimeProvider
import com.avito.utils.ProcessRunner
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths
import java.time.Duration

public abstract class ClearScreenshotsTask : DefaultTask() {

    @get:Input
    public abstract val applicationIdProperty: Property<String>

    @get:Internal
    public abstract val adbPullTimeout: Property<Duration>

    @TaskAction
    public fun clearScreenshots() {
        val applicationId = applicationIdProperty.get()
        val adb = Adb()
        val adbDevicesManager = AdbDevicesManager(
            adb = adb
        )
        val currentDevice = DeviceProviderLocal(
            adbDevicesManager = adbDevicesManager,
            adbDeviceFactory = AdbDeviceFactory(
                loggerFactory = Slf4jGradleLoggerFactory,
                adb = adb,
                timeProvider = DefaultTimeProvider(),
                metricsConfig = null,
                processRunner = ProcessRunner.create(null),
                adbPullTimeout = adbPullTimeout.get(),
            )
        ).getDevice()

        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")
        currentDevice.clearDirectory(remotePath).fold(
            onSuccess = {
                logger.debug("Screenshot directory is cleared up")
            },
            onFailure = {
                logger.warn("Cannot list screenshot directory")
            }
        )
    }
}
