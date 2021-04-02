plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.android)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.runner.service)
    implementation(projects.gradle.runner.shared)
    implementation(projects.common.result)

    gradleTestImplementation(projects.gradle.testProject)
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
