plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
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

    implementation(Dependencies.funktionaleTry)

    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(project(":gradle:git"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:graphite-test-fixtures"))
    testImplementation(project(":common:statsd-test-fixtures"))
    testImplementation(project(":common:logger-test-fixtures"))
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
