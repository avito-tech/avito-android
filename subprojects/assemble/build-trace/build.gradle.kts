plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:assemble:critical-path:api"))
    implementation(project(":subprojects:common:trace-event"))
    implementation(project(":subprojects:assemble:gradle-profile"))
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(gradleApi())

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
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
