plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.subprojects.gradle.impactShared)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.assemble.buildMetricsTracker)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.statsdConfig)

    implementation(libs.antPattern)
    implementation(libs.kotlinGradle)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.subprojects.common.statsd))
    gradleTestImplementation(testFixtures(projects.subprojects.gradle.buildEnvironment))
    gradleTestImplementation(testFixtures(projects.subprojects.gradle.impactShared))
    gradleTestImplementation(testFixtures(projects.subprojects.gradle.impactShared))
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
