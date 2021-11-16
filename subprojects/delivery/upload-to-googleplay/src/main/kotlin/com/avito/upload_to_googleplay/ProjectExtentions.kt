package com.avito.upload_to_googleplay

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers

internal val Project.playConsoleJsonKey by ProjectProperty.lazy { project ->
    val keyPath = project.getOptionalStringProperty("cd.google.play.key.file")
    if (keyPath != null) {
        val keyFile = project.rootProject.file(keyPath)
        require(keyFile.exists()) { "Can't find google play key file in $keyPath" }
        Providers.of(keyFile)
    } else {
        Providers.notDefined()
    }
}
