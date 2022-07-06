plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.testRunner.pluginsConfiguration)
    implementation(projects.subprojects.testRunner.reportViewer)
}

gradlePlugin {
    plugins {
        create("sharedTests") {
            id = "com.avito.android.shared-tests"
            implementationClass = "com.avito.sharedtests.SharedTestsPlugin"
            displayName = "Shared tests"
        }
    }
}
