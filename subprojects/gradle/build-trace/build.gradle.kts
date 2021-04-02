plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.kotlinStdlib)

    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:trace-event"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:build-environment"))
    implementation(gradleApi())

    gradleTestImplementation(project(":gradle:test-project"))
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
