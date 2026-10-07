import org.gradle.api.tasks.PathSensitivity

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.dagger.spi)

    testImplementation(libs.kotlinTestJUnit)
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

// tests provide a jar file to the generated test project
tasks.named("gradleTest") {
    // The generated TestKit project reads dependency versions directly from this catalog.
    inputs.file(rootProject.file("gradle/libs.versions.toml"))
        .withPathSensitivity(PathSensitivity.NONE)
    dependsOn(tasks.named("jar"))
}
