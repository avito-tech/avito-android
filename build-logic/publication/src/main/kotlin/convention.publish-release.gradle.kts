plugins {
    id("convention.publish")
    id("convention.teamcity")
    id("convention.bintray")
}

/**
 * used in ci/publish.sh
 */
tasks.register("publishRelease") {
    group = "publication"

    dependsOn(tasks.named("bintrayUpload"))

    finalizedBy(tasks.named("teamcityPrintReleasedVersion"))
}
