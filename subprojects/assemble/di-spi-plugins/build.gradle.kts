plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.dagger.spi)

    testImplementation(libs.kotlinTestJUnit)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

// tests provide a jar file to the generated test project
tasks.named("gradleTest") {
    dependsOn(tasks.named("jar"))
}
