plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.kotlinStdlib)

    implementation(project(":common:math"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:graphite-config"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:teamcity"))

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(testFixtures(project(":common:graphite")))

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":gradle:git"))
    gradleTestImplementation(testFixtures(project(":common:logger")))
}

gradlePlugin {
    plugins {
        create("buildMetrics") {
            id = "com.avito.android.build-metrics"
            implementationClass = "com.avito.android.plugin.build_metrics.BuildMetricsPlugin"
            displayName = "Build metrics"
        }
    }
}

kotlin {
    explicitApi()
}
