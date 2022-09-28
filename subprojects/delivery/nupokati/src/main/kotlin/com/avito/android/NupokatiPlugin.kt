package com.avito.android

import com.avito.android.gradle_configuration.NupokatiV2Configurator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class NupokatiPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        require(project.plugins.hasPlugin("com.avito.android.qapps")) {
            "Nupokati needs com.avito.android.qapps. Apply it before nupokati"
        }
        val extensionV2 = project.extensions.create<NupokatiExtension>("nupokati")
        val configurator = NupokatiV2Configurator(project, extensionV2)
        configurator.configure()
    }
}
