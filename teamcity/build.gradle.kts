plugins {
    id("kotlin")
    `maven-publish`
}

val teamcityRestClientVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")

    testImplementation(project(":test-project"))
}
