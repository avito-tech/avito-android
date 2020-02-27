plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val teamcityRestClientVersion: String by project

dependencies {
    api("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))

    testImplementation(project(":subprojects:gradle:test-project"))
}
