plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Gradle.androidPlugin)
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
