package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.utils.logging.CILogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.nio.file.Paths

open class ClearScreenshotsTask : DefaultTask() {

    @Input
    val variant = project.objects.property<ApplicationVariant>()

    @Internal
    val ciLogger = CILogger.allToStdout

    @TaskAction
    fun clearScreenshots() {
        val applicationId = variant.get().applicationId
        val currentDevice = getCurrentDevice(ciLogger)
        val remotePath = Paths.get("/sdcard/screenshots/$applicationId")
        currentDevice.clearDirectory(remotePath).onSuccess {
            ciLogger.info("Screenshot directory is cleared up")
        }.onFailure {
            ciLogger.info("Cannot list screenshot directory")
        }
        clearOutputFiles()
    }


    private fun clearOutputFiles() {
        File(project.rootDir.path).listFiles()?.forEach { file ->
            if (!file.isDirectory && file.name.endsWith(".output")) {
                try {
                    file.delete()
                } catch (exception: Exception) {
                    ciLogger.info("Cannot clear ${file.name}, reason ${exception.message}")
                }
            }
        }
        ciLogger.info("Cleared .output files")
    }
}
