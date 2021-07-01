plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.logger.logger)
    api(projects.common.time)

    implementation(projects.gradle.sentryConfig)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.logger.elasticLogger)
    implementation(projects.logger.sentryLogger)
    implementation(projects.logger.slf4jLogger)

    testImplementation(libs.mockitoJUnitJupiter)
}
