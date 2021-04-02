plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
    implementation(project(":common:result"))

    implementation(libs.kotlinStdlib)

    gradleTestImplementation(project(":gradle:test-project"))
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
