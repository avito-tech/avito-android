plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin-legacy")
    id("convention.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:trace-event"))
    implementation(project(":subprojects:gradle:gradle-profile"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(gradleApi())

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(project(":subprojects:gradle:test-project"))
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
