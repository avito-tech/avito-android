plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:trace-event"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":common:composite-exception"))
    implementation(project(":common:graph"))
    implementation(project(":common:result"))
    implementation(libs.gson)
    implementation(gradleApi())

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
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
