plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:tech-budget-common"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:assemble:gradle-profile"))
    implementation(project(":subprojects:assemble:critical-path:api"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:graphite-config"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:worker-extensions"))
    implementation(libs.kotlinGradle)
    implementation(libs.kspGradle)
    implementation(libs.moshi)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(testFixtures(project(":subprojects:gradle:process")))
    testImplementation(testFixtures(project(":subprojects:gradle:build-environment")))

    gradleTestImplementation(project(":subprojects:common:junit-utils"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:gradle:git"))
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
