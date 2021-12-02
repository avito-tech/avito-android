plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.assemble.criticalPath.api)
    implementation(projects.subprojects.common.traceEvent)
    implementation(projects.subprojects.assemble.gradleProfile)
    implementation(projects.subprojects.common.compositeException)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.result)
    implementation(gradleApi())

    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
