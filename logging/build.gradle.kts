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
    implementation(project(":utils"))
    implementation(project(":sentry"))
    implementation(project(":kotlin-dsl-support"))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation(project(":utils"))
}
