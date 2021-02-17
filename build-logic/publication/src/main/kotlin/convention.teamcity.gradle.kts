plugins {
    id("convention.publish-base")
}

/**
 * https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ReportingBuildNumber
 */
tasks.register("teamcityPrintReleasedVersion") {
    group = "publication"
    description = "Prints teamcity service message to display released version as build number"

    doLast {
        logger.lifecycle("##teamcity[buildNumber '$version']")
    }
}
