plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-gradle-plugin")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:gradle-logger"))

    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:time"))

    implementation(libs.gson)
    implementation(libs.kotson)

    testImplementation(project(":subprojects:gradle:test-project"))
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
