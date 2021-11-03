plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.buildFailer)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.impactShared)
    implementation(projects.subprojects.gradle.buildMetricsTracker)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(libs.kotlinGradle)
    implementation(libs.layoutLibApi) {
        because("com.android.resources.ResourceType")
    }
    implementation(libs.androidSdkCommon) {
        because("com.android.ide.common.symbols.SymbolTable")
    }

    // for test
    implementation(projects.subprojects.gradle.moduleTypes)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
