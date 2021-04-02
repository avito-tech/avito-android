plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.common.logger)
    api(projects.common.time)

    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.common.elasticLogger)
    implementation(projects.common.sentryLogger)
    implementation(projects.common.slf4jLogger)

    testImplementation(libs.mockitoJUnitJupiter)
}
