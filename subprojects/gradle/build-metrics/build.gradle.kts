plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.common.math)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.gradleProfile)
    implementation(projects.gradle.criticalPath.api)
    implementation(projects.gradle.buildMetricsTracker)
    implementation(projects.gradle.android)
    implementation(projects.gradle.graphiteConfig)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.teamcity)
    implementation(libs.kotlinPlugin)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(testFixtures(projects.common.graphite))
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.statsd))
    testImplementation(testFixtures(projects.gradle.buildEnvironment))

    gradleTestImplementation(projects.common.junitUtils)
    gradleTestImplementation(projects.common.testOkhttp)
    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.gradle.git)
    gradleTestImplementation(testFixtures(projects.logger.logger))
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
