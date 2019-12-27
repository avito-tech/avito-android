package com.avito.utils.gradle

import org.gradle.api.Project

@JvmOverloads
fun Project.getStringProperty(name: String, default: String? = null): String? {
    return if (hasProperty(name)) {
        property(name)?.toString() ?: default
    } else {
        default
    }
}

@JvmOverloads
fun Project.getBooleanProperty(name: String, default: Boolean = false): Boolean {
    return if (hasProperty(name)) {
        property(name)?.toString()?.toBoolean() ?: default
    } else {
        default
    }
}

@JvmOverloads
fun Project.getFloatProperty(name: String, default: Float? = null): Float? {
    return if (hasProperty(name)) {
        try {
            property(name)?.toString()?.toFloat() ?: default
        } catch (e: NumberFormatException) {
            default
        }
    } else {
        default
    }
}

fun Project.isRoot() = (project == project.rootProject)

/**
 * @param task The dependency for build (task provider or path)
 */
fun Project.addPreBuildTasks(vararg task: Any) {
    addPreBuildTasksIfAndroidLibrary(task)
    addPreBuildTasksIfApplication(task)
    addPreBuildTasksIfJvm(task)
}

/**
 * @param task The dependency for build (task provider or path)
 */
fun Project.addPreBuildTasksIfAndroidLibrary(vararg task: Any) {
    pluginManager.withPlugin("com.android.library") {
        tasks.named("preBuild").configure {
            it.dependsOn(task)
        }
    }
}

/**
 * @param task The dependency for build (task provider or path)
 */
fun Project.addPreBuildTasksIfApplication(vararg task: Any) {
    pluginManager.withPlugin("com.android.application") {
        tasks.named("preBuild").configure {
            it.dependsOn(task)
        }
    }
}

/**
 * @param task The dependency for build (task provider or path)
 */
fun Project.addPreBuildTasksIfJvm(vararg task: Any) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.named("jar").configure {
            it.dependsOn(task)
        }
    }
}
