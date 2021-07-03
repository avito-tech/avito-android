package com.avito.android

import org.gradle.api.Project

/**
 * @param task The dependency for build (task provider or path)
 */
public fun Project.addPreBuildTasks(vararg task: Any) {
    addPreBuildTasksIfAndroidLibrary(task)
    addPreBuildTasksIfApplication(task)
    addPreBuildTasksIfJvm(task)
}

/**
 * @param task The dependency for build (task provider or path)
 */
public fun Project.addPreBuildTasksIfAndroidLibrary(vararg task: Any) {
    pluginManager.withPlugin("com.android.library") {
        tasks.named("preBuild").configure {
            it.dependsOn(task)
        }
    }
}

/**
 * @param task The dependency for build (task provider or path)
 */
public fun Project.addPreBuildTasksIfApplication(vararg task: Any) {
    pluginManager.withPlugin("com.android.application") {
        tasks.named("preBuild").configure {
            it.dependsOn(task)
        }
    }
}

/**
 * @param task The dependency for build (task provider or path)
 */
public fun Project.addPreBuildTasksIfJvm(vararg task: Any) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.named("jar").configure {
            it.dependsOn(task)
        }
    }
}
