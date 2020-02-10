plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val teamcityRestClientVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
}
