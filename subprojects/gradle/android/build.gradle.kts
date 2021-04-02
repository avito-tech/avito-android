plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.androidGradlePlugin) {
        excludeTransitiveAgpDependencies()
    }

    api(project(":common:result"))

    implementation(libs.kotlinStdlib)
    implementation(gradleApi())
    implementation(project(":common:files"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:process-test-fixtures"))
}

/**
 * causes warning: Runtime JAR files in the classpath should have the same version
 * consider adding explicit dependencies with project's kotlin version in case of runtime errors
 */
fun ExternalModuleDependency.excludeTransitiveAgpDependencies() {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
}
