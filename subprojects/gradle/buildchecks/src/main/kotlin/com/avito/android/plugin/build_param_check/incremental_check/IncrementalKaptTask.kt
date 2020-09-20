package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.android.plugin.build_param_check.CheckResult
import com.avito.android.plugin.build_param_check.CheckTaskWithMode
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Project
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
        val subProject = project.subprojects.firstOrNull {
            it.hasKotlinKapt && it.hasRoomKapt
        }
        if (subProject != null) {
            mode.get().check(subProject) {
                if (RoomIncrementalKaptChecker(subProject).isSupported()) {
                    CheckResult.Ok
                } else {
                    CheckResult.Failed(collectErrorMessage())
                }
            }
        }
    }

    private fun collectErrorMessage() = """
        Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions. 
        You have to use JDK 11 and higher or embedded one in Android Studio 3.5.0-beta02 and higher.
        Current JDK is ${System.getProperty("java.runtime.version")} provided by ${System.getProperty("java.vendor")}.
        https://avito-tech.github.io/avito-android/docs/projects/buildchecks/#room
    """.trimIndent()

    private val Project.hasKotlinKapt: Boolean
        get() = plugins.hasPlugin("kotlin-kapt")

    private val Project.hasRoomKapt: Boolean
        get() = configurations.findByName("kapt")?.dependencies?.any {
            it.group == "androidx.room" && it.name == "room-compiler"
        } ?: false

}
