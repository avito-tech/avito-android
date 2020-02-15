plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val jslackVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.github.seratch:jslack-api-client:$jslackVersion")
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation(project(":subprojects:gradle:utils"))
}

configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:$okhttpVersion")
    }
}
