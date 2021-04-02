plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
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
    implementation(project(":common:result"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(libs.kotlinStdlib)
    implementation(libs.kotlinPlugin)

    // for test
    implementation(project(":gradle:module-types"))
    implementation(project(":gradle:room-config"))

    gradleTestImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.build-checks"
            implementationClass = "com.avito.android.build_checks.BuildParamCheckPlugin"
            displayName = "Build checks"
        }
    }
}

kotlin {
    explicitApi()
}
