plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.gradle.impactShared)

    implementation(gradleApi())
    implementation(projects.gradle.android)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.common.files)
    implementation(projects.common.math)
    implementation(projects.gradle.git)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.statsdConfig)

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    gradleTestImplementation(testFixtures(projects.gradle.buildEnvironment))
    gradleTestImplementation(testFixtures(projects.gradle.impactShared))
    gradleTestImplementation(testFixtures(projects.common.statsd))
    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.gradle.impactSharedTestFixtures)
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
