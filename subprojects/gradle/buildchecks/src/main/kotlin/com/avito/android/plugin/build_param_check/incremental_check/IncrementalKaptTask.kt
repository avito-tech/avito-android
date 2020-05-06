package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.android.plugin.build_param_check.CheckResult
import com.avito.android.plugin.build_param_check.CheckTaskWithMode
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.tasks.TaskAction

internal abstract class IncrementalKaptTask : CheckTaskWithMode() {

    @TaskAction
    fun check() {
        val incrementalKaptEnabled = project.getBooleanProperty("kapt.incremental.apt", default = false)
        if (incrementalKaptEnabled) {
            checkAnnotationProcessors()
        }
    }

    private fun checkAnnotationProcessors() {
        project.subprojects.forEach { subproject ->
            if (subproject.plugins.hasPlugin("kotlin-kapt")) {
                mode.get().check(subproject) {
                    if (RoomIncrementalKaptChecker(subproject).isSupported()) {
                        CheckResult.Ok
                    } else {
                        CheckResult.Failed(collectErrorMessage())
                    }
                }
            }
        }
    }

    private fun collectErrorMessage() = """
        Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions. 
        You have to use JDK embedded in Android Studio 3.5.0-beta02 and higher.
        Current JDK is ${System.getProperty("java.runtime.version")} provided by ${System.getProperty("java.vendor")}.
    """.trimIndent()
}
