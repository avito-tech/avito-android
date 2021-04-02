plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.buildFailer)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.android)
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.buildMetrics)
    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.common.files)
    implementation(projects.common.result)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.gradleExtensions)
    implementation(libs.kotlinPlugin)

    // for test
    implementation(projects.gradle.moduleTypes)
    implementation(projects.gradle.roomConfig)

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.build-checks"
            implementationClass = "com.avito.android.build_checks.BuildParamCheckPlugin"
            displayName = "Build checks"
        }
    }
}

kotlin {
    explicitApi()
}
