plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.emcee.action)
    implementation(libs.androidGradle)
    implementation(libs.moshiKotlin)
    implementation(libs.kotlinReflect) {
        because("moshi 1.12.0 depend on 1.4.x kotlin, and 1.13 on 1.6.x, we use 1.5")
    }

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
