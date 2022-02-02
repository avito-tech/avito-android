plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.emcee.action)
    implementation(libs.androidGradle)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("emcee") {
            id = "com.avito.android.emcee"
            implementationClass = "com.avito.emcee.EmceePlugin"
            displayName = "Plugin to run instrumentation tests with Emcee test runner"
        }
    }
}
