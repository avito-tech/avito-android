package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.nio.file.Paths

open class ClearScreenshotsTask : DefaultTask() {

    @Input
    val variant = project.objects.property<ApplicationVariant>()

    @Internal
    val ciLogger = CILogger.allToStdout

    @TaskAction
    fun clearScreenshots() {
        val applicationId = variant.get().applicationId
        val adb = Adb()
        val adbDevicesManager = AdbDevicesManager(StdOutLogger(), adb = adb)
        val currentDevice = DeviceProviderLocal(adb, adbDevicesManager, ciLogger).getDevice()

        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")
        currentDevice.clearDirectory(remotePath).onSuccess {
            ciLogger.info("Screenshot directory is cleared up")
        }.onFailure {
            ciLogger.info("Cannot list screenshot directory")
        }
    }
}
