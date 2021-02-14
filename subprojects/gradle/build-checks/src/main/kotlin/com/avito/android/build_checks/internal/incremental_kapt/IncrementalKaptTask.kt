package com.avito.android.build_checks.internal.incremental_kapt

import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.android.build_checks.internal.CheckResult
import com.avito.android.build_checks.internal.CheckTaskWithMode
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.utils.BuildFailer
import com.avito.utils.buildFailer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

internal abstract class IncrementalKaptTask @Inject constructor(objects: ObjectFactory) : CheckTaskWithMode() {

    private val Project.hasKotlinKapt: Boolean
        get() = plugins.hasPlugin("kotlin-kapt")

    private val Project.hasRoomKapt: Boolean
        get() = configurations.findByName("kapt")?.dependencies?.any {
            it.group == "androidx.room" && it.name == "room-compiler"
        } ?: false

    @get:Internal
    val accessor = objects.property<BuildEnvironmentInfo>()

    @TaskAction
    fun check() {
        val buildFailer = project.buildFailer
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val incrementalKaptEnabled = project.getBooleanProperty("kapt.incremental.apt", default = false)
        if (incrementalKaptEnabled) {
            checkAnnotationProcessors(
                failer = buildFailer,
                loggerFactory = loggerFactory,
                envInfo = accessor.get()
            )
        }
    }

    private fun checkAnnotationProcessors(
        failer: BuildFailer,
        loggerFactory: LoggerFactory,
        envInfo: BuildEnvironmentInfo
    ) {
        val subProject = project.subprojects.firstOrNull {
            it.hasKotlinKapt && it.hasRoomKapt
        }
        if (subProject != null) {
            mode.get().check(buildFailer = failer, logger = loggerFactory.create("CheckAnnotationProcessors")) {
                if (RoomIncrementalKaptChecker(subProject, loggerFactory = loggerFactory).isSupported()) {
                    CheckResult.Ok
                } else {
                    CheckResult.Failed(collectErrorMessage(envInfo))
                }
            }
        }
    }

    private fun collectErrorMessage(envInfo: BuildEnvironmentInfo) = """
        Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions. 
        You have to use JDK 11 and higher or embedded one in Android Studio 3.5.0-beta02 and higher.
        Current JDK is ${envInfo.javaInfo}.
        https://avito-tech.github.io/avito-android/docs/projects/buildchecks/#room
    """.trimIndent()
}
