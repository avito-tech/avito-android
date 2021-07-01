plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

@Suppress("UnstableApiUsage")
val androidGradlePluginVersion = providers.systemProperty("androidGradlePluginVersion").forUseAtConfigurationTime()

dependencies {
    api(libs.androidGradlePlugin) {
        excludeTransitiveAgpDependencies()
    }

    api(project(":common:result"))

    implementation(gradleApi())
    implementation(project(":common:files"))
    implementation(project(":gradle:process"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":gradle:process")))
}

/**
 * causes warning: Runtime JAR files in the classpath should have the same version
 * consider adding explicit dependencies with project's kotlin version in case of runtime errors
 */
fun ExternalModuleDependency.excludeTransitiveAgpDependencies() {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
}
