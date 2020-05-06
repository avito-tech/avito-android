package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import java.net.URLClassLoader
import javax.annotation.processing.AbstractProcessor

internal class RoomIncrementalKaptChecker(
    private val project: Project
) {

    fun isSupported(): Boolean {
        val processor = createRoomProcessor()

        try {
            // Trying the first approach when the annotation processor is asked for supported options
            // like it does in the real compilation process
            return checkCommonWay(processor)
        } catch (e: Exception) {
            System.err.println("Failed to check Room for KAPT incremental support in official way: %e")
        }

        try {
            return checkReflectionWay(processor)
        } catch (e: Exception) {
            System.err.println("Failed to check Room for KAPT incremental support in reflection way: %e")
        }

        throw IllegalStateException("Failed to check Room for KAPT incremental support")
    }

    private fun createRoomProcessor(): AbstractProcessor =
        getKaptClassLoader().loadClass("androidx.room.RoomProcessor").newInstance() as AbstractProcessor


    private fun getKaptClassLoader(): ClassLoader {
        val task = project.tasks.withType<KaptGenerateStubsTask>().firstOrNull {
            !it.name.contains("Test") && !it.kaptClasspath.isEmpty
        }
        require(task != null) { "Failed to find KaptGenerateStubsTask task" }
        return URLClassLoader(task.kaptClasspath.map { it.toURI().toURL() }.toTypedArray())
    }

    private fun checkCommonWay(processor: AbstractProcessor): Boolean {
        processor.init(
            FakeProcessingEnvironment(
                options = mapOf("room.incremental" to "true"),
                logTag = "RoomMessage"
            )
        )
        return processor.supportedOptions.contains(ISOLATING_ANNOTATION_PROCESSORS_INDICATOR)
    }

    private fun checkReflectionWay(processor: AbstractProcessor): Boolean {
        // https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/RoomProcessor.kt
        val method = processor::class.java.getDeclaredMethod("methodParametersVisibleInClassFiles").apply {
            isAccessible = true
        }
        return method.invoke(processor) as Boolean
    }

}

private const val ISOLATING_ANNOTATION_PROCESSORS_INDICATOR = "org.gradle.annotation.processing.isolating"
