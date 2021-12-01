plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.androidGradle) {
        excludeTransitiveAgpDependencies()
    }

    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.gradle.process))
}

/**
 * causes warning: Runtime JAR files in the classpath should have the same version
 * consider adding explicit dependencies with project's kotlin version in case of runtime errors
 */
fun ExternalModuleDependency.excludeTransitiveAgpDependencies() {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
}
