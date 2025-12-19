package com.avito.android

import com.avito.android.gradle_configuration.NupokatiV2Configurator
import com.avito.android.gradle_configuration.NupokatiV4Configurator
import com.avito.android.gradle_configuration.extension.NupokatiExtension
import com.avito.android.gradle_configuration.extension.spec.NupokatiV2PipelineSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV4PipelineSpec
import org.gradle.api.Plugin
import org.gradle.api.Project

public class NupokatiPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "nupokati",
            NupokatiExtension::class.java,
            project.objects
        )
        extension.pipelines.withType(NupokatiV2PipelineSpec::class.java).all { spec ->
            require(project.plugins.hasPlugin("com.avito.android.qapps")) {
                "NupokatiV2 needs com.avito.android.qapps. Apply it before nupokati"
            }
            NupokatiV2Configurator(project = project, pipelineSpec = spec).configure()
        }
        extension.pipelines.withType(NupokatiV4PipelineSpec::class.java).all { spec ->
            NupokatiV4Configurator(project = project, pipelineSpec = spec).configure()
        }
    }
}
