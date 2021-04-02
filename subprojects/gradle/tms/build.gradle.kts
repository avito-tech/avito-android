plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:statsd-config"))

    implementation(project(":common:report-viewer"))
    implementation(project(":common:time"))
    implementation(project(":common:http-client"))

    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.kotlinStdlib)

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
