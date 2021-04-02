plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(projects.common.coroutinesExtension)
    api(projects.common.statsd)
    api(projects.common.time)
    implementation(projects.gradle.runner.shared)
    implementation(projects.common.result)
    implementation(libs.ddmlib)
    implementation(libs.rxJava)

    testImplementation(testFixtures(projects.common.logger))
    testImplementation(testFixtures(projects.common.time))
    testImplementation(projects.common.files)
    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.resources)
    testImplementation(projects.gradle.testProject)
    testImplementation(projects.gradle.runner.sharedTest)
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)
}
