plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":android"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":runner:service"))
    implementation(project(":runner:shared"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
}

gradlePlugin {
    plugins {
        create("designScreenshots") {
            id = "com.avito.android.design-screenshots"
            implementationClass = "com.avito.plugin.ScreenshotsPlugin"
        }
    }
}
