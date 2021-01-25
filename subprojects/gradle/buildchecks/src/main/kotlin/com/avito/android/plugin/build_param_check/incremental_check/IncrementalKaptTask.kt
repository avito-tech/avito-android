package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.android.plugin.build_param_check.CheckResult
import com.avito.android.plugin.build_param_check.CheckTaskWithMode
import com.avito.android.plugin.build_param_check.SystemValuesAccessor
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
    val accessor = objects.property<SystemValuesAccessor>()

    @TaskAction
    fun check() {
        val buildFailer = project.buildFailer
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val incrementalKaptEnabled = project.getBooleanProperty("kapt.incremental.apt", default = false)
        if (incrementalKaptEnabled) {
            checkAnnotationProcessors(
                failer = buildFailer,
                loggerFactory = loggerFactory,
                accessor = accessor.get()
            )
        }
    }

    private fun checkAnnotationProcessors(
        failer: BuildFailer,
        loggerFactory: LoggerFactory,
        accessor: SystemValuesAccessor
    ) {
        val subProject = project.subprojects.firstOrNull {
            it.hasKotlinKapt && it.hasRoomKapt
        }
        if (subProject != null) {
            mode.get().check(buildFailer = failer, logger = loggerFactory.create("CheckAnnotationProcessors")) {
                if (RoomIncrementalKaptChecker(subProject, loggerFactory = loggerFactory).isSupported()) {
                    CheckResult.Ok
                } else {
                    CheckResult.Failed(collectErrorMessage(accessor))
                }
            }
        }
    }

    private fun collectErrorMessage(accessor: SystemValuesAccessor) = """
        Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions. 
        You have to use JDK 11 and higher or embedded one in Android Studio 3.5.0-beta02 and higher.
        Current JDK is ${accessor.javaInfo}.
        https://avito-tech.github.io/avito-android/docs/projects/buildchecks/#room
    """.trimIndent()
}
