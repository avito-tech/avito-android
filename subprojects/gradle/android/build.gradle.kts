plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

@Suppress("UnstableApiUsage")
val androidGradlePluginVersion = providers.systemProperty("androidGradlePluginVersion").forUseAtConfigurationTime()

dependencies {
    api("com.android.tools.build:gradle:${androidGradlePluginVersion.get()}") {
        excludeTransitiveAgpDependencies()
    }

    implementation(gradleApi())
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:process-test-fixtures"))
}

/**
 * causes warning: Runtime JAR files in the classpath should have the same version
 * consider adding explicit dependencies with project's kotlin version in case of runtime errors
 */
fun ExternalModuleDependency.excludeTransitiveAgpDependencies() {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
}
