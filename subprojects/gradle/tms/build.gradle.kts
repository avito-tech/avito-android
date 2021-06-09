plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:statsd-config"))

    implementation(project(":test-runner:report-viewer"))
    implementation(project(":common:time"))
    implementation(project(":common:http-client"))

    implementation(libs.gson)
    implementation(libs.kotson)

    gradleTestImplementation(project(":gradle:test-project"))
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
