plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:gradle-logger"))

    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:time"))

    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)

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
