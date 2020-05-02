plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:graphite-config"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:gradle-profile"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
    testImplementation(project(":subprojects:gradle:git"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:graphite-test-fixtures"))
    testImplementation(project(":subprojects:common:statsd-test-fixtures"))
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
