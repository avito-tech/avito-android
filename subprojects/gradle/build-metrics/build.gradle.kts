plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:graphite-config"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:gradle-profile"))
    implementation(project(":gradle:build-environment"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
    testImplementation(project(":gradle:git"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:graphite-test-fixtures"))
    testImplementation(project(":common:statsd-test-fixtures"))
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
