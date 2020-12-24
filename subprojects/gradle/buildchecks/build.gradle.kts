plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:build-metrics"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":common:files"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(Dependencies.Gradle.kotlinPlugin)
    implementation(Dependencies.funktionaleTry)

    // for test
    implementation(project(":gradle:module-types"))
    implementation(project(":gradle:room-config"))

    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.buildchecks"
            implementationClass = "com.avito.android.plugin.build_param_check.BuildParamCheckPlugin"
            displayName = "Build checks"
        }
    }
}
