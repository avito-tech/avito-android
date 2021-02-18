plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-profile"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:graphite-config"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:teamcity"))

    implementation(libs.funktionaleTry)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)

    testImplementation(testFixtures(project(":subprojects:common:graphite")))
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
