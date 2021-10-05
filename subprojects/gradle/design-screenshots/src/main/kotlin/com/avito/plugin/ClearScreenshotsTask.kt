package com.avito.plugin

import com.avito.logger.LoggerFactory
import com.avito.logger.create
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

public abstract class ClearScreenshotsTask : DefaultTask() {

    @get:Input
    public abstract val applicationIdProperty: Property<String>

    @get:Internal
    public abstract val loggerFactory: Property<LoggerFactory>

    @TaskAction
    public fun clearScreenshots() {
        val loggerFactory = loggerFactory.get()
        val logger = loggerFactory.create<ClearScreenshotsTask>()

        val applicationId = applicationIdProperty.get()
        val adb = Adb()
        val adbDevicesManager = AdbDevicesManager(
            loggerFactory = loggerFactory,
            adb = adb
        )
        val currentDevice = DeviceProviderLocal(
            adbDevicesManager = adbDevicesManager,
            adbDeviceFactory = AdbDeviceFactory(
                loggerFactory = loggerFactory,
                adb = adb,
                timeProvider = DefaultTimeProvider(),
                metricsConfig = null,
                processRunner = ProcessRunner.create(null)
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
