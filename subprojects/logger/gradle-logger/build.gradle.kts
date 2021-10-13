plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.subprojects.logger.logger)
    api(projects.subprojects.common.time)

    implementation(projects.subprojects.gradle.sentryConfig)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.logger.sentryLogger)
    implementation(projects.subprojects.logger.slf4jLogger)

    testImplementation(libs.mockitoJUnitJupiter)
}
