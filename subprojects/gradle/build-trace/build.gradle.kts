plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.traceEvent)
    implementation(projects.gradle.gradleProfile)
    implementation(projects.gradle.buildEnvironment)
    implementation(gradleApi())

    gradleTestImplementation(projects.gradle.testProject)
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
