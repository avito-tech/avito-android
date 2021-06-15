package com.avito.instrumentation.internal

import com.avito.utils.gradle.envArgs
import org.gradle.api.Project

internal object BuildEnvResolver {

    fun getBuildId(project: Project): String {
        return project.envArgs.build.id.toString()
    }

    fun getBuildType(project: Project): String {
        return project.envArgs.build.type
    }
}
