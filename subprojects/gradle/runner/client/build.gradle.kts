plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(projects.gradle.runner.shared)
    api(projects.gradle.runner.service)

    implementation(projects.gradle.traceEvent)
    implementation(projects.common.math)
    implementation(projects.common.result)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.runner.sharedTest)
    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(testFixtures(projects.common.time))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
