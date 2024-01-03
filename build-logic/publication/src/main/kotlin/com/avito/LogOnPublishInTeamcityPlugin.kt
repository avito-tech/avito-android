package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project

class LogOnPublishInTeamcityPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishBasePlugin::class.java)
            /**
             * https://www.jetbrains.com/help/teamcity/service-messages.html#Reporting+Build+Number
             */
            tasks.register("teamcityPrintReleasedVersion") {
                it.group = "publication"
                it.description = "Prints teamcity service message to display released version as build number"

                it.doLast {
                    logger.lifecycle("##teamcity[buildNumber '$version']")
                }
            }
        }
    }

}
