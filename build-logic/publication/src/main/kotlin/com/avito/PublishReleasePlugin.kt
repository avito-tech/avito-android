package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project

class PublishReleasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishBasePlugin::class.java)
            plugins.apply(LogOnPublishInTeamcityPlugin::class.java)
            plugins.apply(PublishSonatypePlugin::class.java)
            plugins.apply(PublishArtifactoryPlugin::class.java)

            /**
             * used in ci/publish.sh
             */
            tasks.register("publishRelease") {
                it.group = "publication"
                it.dependsOn(tasks.named("publishToSonatype"))
                it.finalizedBy(tasks.named("teamcityPrintReleasedVersion"))
            }
        }
    }
}
