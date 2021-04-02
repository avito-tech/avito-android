plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.common.math)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.gradleProfile)
    implementation(projects.gradle.android)
    implementation(projects.gradle.graphiteConfig)
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.teamcity)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(testFixtures(projects.common.graphite))

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.gradle.git)
    gradleTestImplementation(testFixtures(projects.common.logger))
}

gradlePlugin {
    plugins {
        create("buildMetrics") {
            id = "com.avito.android.build-metrics"
            implementationClass = "com.avito.android.plugin.build_metrics.BuildMetricsPlugin"
            displayName = "Build metrics"
        }
    }
}

kotlin {
    explicitApi()
}
