plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))

    implementation(project(":common:report-viewer"))
    implementation(project(":common:time"))

    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
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
