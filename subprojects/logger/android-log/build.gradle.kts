plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.logger.logger)

    implementation(projects.logger.elasticLogger)
    implementation(projects.logger.sentryLogger)
}
