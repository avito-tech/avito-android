plugins {
    id("kotlin")
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project

dependencies {
    api("com.android.tools.build:gradle:$androidGradlePluginVersion")

    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))

    testImplementation(testFixtures(project(":utils")))
}
