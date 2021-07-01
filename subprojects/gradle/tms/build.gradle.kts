plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.testRunner.reportViewer)
    implementation(projects.common.time)
    implementation(projects.common.problem)
    implementation(projects.common.httpClient)

    implementation(libs.gson)
    implementation(libs.kotson)
}

gradlePlugin {
    plugins {
        create("tms") {
            id = "com.avito.android.tms"
            implementationClass = "com.avito.plugin.TmsPlugin"
            displayName = "Avito Test Management System integration plugin"
        }
    }
}
