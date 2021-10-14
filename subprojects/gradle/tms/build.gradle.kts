plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.testRunner.reportViewer)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.httpClient)

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
