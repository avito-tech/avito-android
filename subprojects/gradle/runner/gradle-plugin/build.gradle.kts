plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:kubernetes"))
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("testRunner") {
            id = "com.avito.android.test-runner"
            implementationClass = "com.avito.android.testrunner.TestRunnerPlugin"
            description = "Avito Android Test Runner"
        }
    }
}
