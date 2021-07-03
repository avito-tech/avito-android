plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.criticalPath.api)
    implementation(projects.gradle.traceEvent)
    implementation(projects.gradle.gradleProfile)
    implementation(projects.common.compositeException)
    implementation(projects.common.problem)
    implementation(projects.common.result)
    implementation(gradleApi())

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.logger.logger))
}

gradlePlugin {
    plugins {
        create("buildTrace") {
            id = "com.avito.android.build-trace"
            implementationClass = "com.avito.android.build_trace.BuildTracePlugin"
            displayName = "Build trace"
        }
    }
}
