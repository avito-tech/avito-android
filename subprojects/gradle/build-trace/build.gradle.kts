plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:trace-event"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:build-environment"))
    implementation(gradleApi())

    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(project(":gradle:test-project"))
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
