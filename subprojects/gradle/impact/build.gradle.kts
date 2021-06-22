import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.configurationcache.extensions.serviceOf

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":gradle:impact-shared"))

    implementation(gradleApi())
    implementation(project(":gradle:android"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":common:files"))
    implementation(project(":common:math"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:build-metrics-tracker"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:statsd-config"))

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    gradleTestImplementation(testFixtures(project(":gradle:build-environment")))
    gradleTestImplementation(testFixtures(project(":gradle:impact-shared")))
    gradleTestImplementation(testFixtures(project(":common:statsd")))
    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":gradle:impact-shared-test-fixtures"))

    // workaround for https://github.com/gradle/gradle/issues/16774
    gradleTestRuntimeOnly(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders").classpath.asFiles.first()
        )
    )
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
