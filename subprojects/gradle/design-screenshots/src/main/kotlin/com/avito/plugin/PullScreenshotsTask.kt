package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.runner.ProcessNotification
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.nio.file.Paths

open class PullScreenshotsTask : DefaultTask() {

    @Input
    val variant = project.objects.property<ApplicationVariant>()

    @Internal
    val ciLogger = CILogger.allToStdout

    @TaskAction
    fun pullScreenshots() {
        val applicationId = variant.get().testVariant.applicationId
        val adb = Adb()
        val adbDevicesManager = AdbDevicesManager(StdOutLogger(), adb = adb)
        val currentDevice = DeviceProviderLocal(adb, adbDevicesManager, ciLogger).getDevice()

        val referencePath = Paths.get("${project.projectDir.path}/src/androidTest/assets/screenshots/")
        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")
        currentDevice.list(remotePath.toString()).onSuccess { result ->
            if (result is ProcessNotification.Exit) {
                result.output.lines().firstOrNull { it.trim().isNotEmpty() }
                    ?.let { directory ->
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

    private fun clearOutputFiles() {
        File(project.rootDir.path).listFiles()?.forEach { file ->
            if (!file.isDirectory && file.name.endsWith(".output")) {
                file.delete()
            }
        }
        ciLogger.info("Cleared .output files")
    }
}
