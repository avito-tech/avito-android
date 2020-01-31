plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val jslackVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.github.seratch:jslack:$jslackVersion")
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation(project(":subprojects:gradle:utils"))
}
