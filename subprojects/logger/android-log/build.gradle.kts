plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.logger.logger)

    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.logger.sentryLogger)
}
