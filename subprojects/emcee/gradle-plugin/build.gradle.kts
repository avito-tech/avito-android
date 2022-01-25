plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    implementation(projects.subprojects.emcee.action)
}

gradlePlugin {
    plugins {
        create("emcee") {
            id = "com.avito.android.emcee-plugin"
            implementationClass = "com.avito.emcee.EmceePlugin"
            displayName = "Plugin to run instrumentation tests with Emcee test runner"
        }
    }
}
