plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
}

gradlePlugin {
    plugins {
        create("designScreenshots") {
            id = "com.avito.android.design-screenshots"
            implementationClass = "com.avito.plugin.ScreenshotsPlugin"
            displayName = "Screenshot testing"
        }
    }
}
