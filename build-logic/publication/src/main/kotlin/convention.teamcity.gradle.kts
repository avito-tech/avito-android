plugins {
    id("convention.publish-base")
}

/**
 * https://www.jetbrains.com/help/teamcity/service-messages.html#Reporting+Build+Number
 */
tasks.register("teamcityPrintReleasedVersion") {
    group = "publication"
    description = "Prints teamcity service message to display released version as build number"

    doLast {
        logger.lifecycle("##teamcity[buildNumber '$version']")
    }
}
