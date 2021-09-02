plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.buildFailer)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.android)
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.buildMetricsTracker)
    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.common.files)
    implementation(projects.common.result)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.gradleExtensions)
    implementation(libs.kotlinGradle)
    implementation(libs.layoutLibApi) {
        because("com.android.resources.ResourceType")
    }
    implementation(libs.androidSdkCommon) {
        because("com.android.ide.common.symbols.SymbolTable")
    }

    // for test
    implementation(projects.gradle.moduleTypes)

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
