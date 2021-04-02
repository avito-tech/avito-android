plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.androidGradlePlugin) {
        excludeTransitiveAgpDependencies()
    }

    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.common.files)
    implementation(projects.gradle.process)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.processTestFixtures)
}

/**
 * causes warning: Runtime JAR files in the classpath should have the same version
 * consider adding explicit dependencies with project's kotlin version in case of runtime errors
 */
fun ExternalModuleDependency.excludeTransitiveAgpDependencies() {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
}
