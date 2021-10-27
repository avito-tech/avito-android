package com.avito.instrumentation.internal

import com.avito.utils.gradle.envArgs
import org.gradle.api.Project

internal class BuildEnvResolver(private val project: Project) {

    fun getBuildId(): String {
        return project.envArgs.build.id.toString()
    }

    fun getBuildType(): String {
        return project.envArgs.build.type
    }
}
