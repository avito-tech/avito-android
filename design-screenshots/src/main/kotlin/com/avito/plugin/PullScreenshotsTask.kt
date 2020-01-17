package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.runner.ProcessNotification
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

open class PullScreenshotsTask : DefaultTask() {

    @Internal
    var variant: ApplicationVariant? = null

    @Internal
    val ciLogger = CILogger.allToStdout

    @TaskAction
    fun pullScreenshots() {
        val applicationId = variant?.testVariant?.applicationId ?: return
        val currentDevice = getCurrentDevice() ?: return
        val referencePath = Paths.get("${project.projectDir.path}/src/androidTest/assets/screenshots/")
        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")
        currentDevice.list(remotePath.toString()).onSuccess { result ->
            if (result is ProcessNotification.Exit) {
                result.output.lines().firstOrNull { it.trim().isNotEmpty() }?.let { directory ->
                    if (directory.trim().isNotEmpty()) {
                        referencePath.toFile().mkdirs()
                        val remoteEmulatorPath = remotePath.resolve(directory)
                        currentDevice.pull(
                            from = remoteEmulatorPath,
                            to = referencePath
                        )
                    }
                }
            }
            ciLogger.info("Screenshots are pulled to $referencePath")
            clearOutputFiles()
        }.onFailure {
            ciLogger.info("Cannot list screenshot directory")
        }
    }

    private fun getCurrentDevice(): AdbDevice? {
        val adbDevicesManager = AdbDevicesManager(StdOutLogger())
        adbDevicesManager.connectedDevices().let { set ->
            return when (set.size) {
                0 -> {
                    val exception = Exception("There are no connected devices")
                    ciLogger.critical("There are no connected devices", exception)
                    throw exception
                }
                1 -> {
                    ciLogger.info("One device found, gonna start pulling")
                    set.first() as? AdbDevice
                }
                else -> {
                    val exception =
                        Exception("There are too much devices, turn them off until there will be one device only")
                    ciLogger.critical(
                        "There are too much devices, turn them off until there will be one device only",
                        exception
                    )
                    throw exception
                }
            }
        }
    }

    private fun clearOutputFiles() {
        File(project.rootDir.path).listFiles()?.forEach { file ->
            if (!file.isDirectory && file.name.endsWith(".output")) {
                file.delete()
            }
        }
        ciLogger.info("Cleared .output files")
    }
}
