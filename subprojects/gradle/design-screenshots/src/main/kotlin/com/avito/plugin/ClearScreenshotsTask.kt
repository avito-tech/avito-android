package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.time.DefaultTimeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.nio.file.Paths

abstract class ClearScreenshotsTask : DefaultTask() {

    @Input
    val variant = project.objects.property<ApplicationVariant>()

    @TaskAction
    fun clearScreenshots() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val logger = loggerFactory.create<ClearScreenshotsTask>()

        val applicationId = variant.get().applicationId
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
                metricsConfig = null
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
