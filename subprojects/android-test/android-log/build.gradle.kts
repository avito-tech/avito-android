plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(projects.common.logger)

    implementation(projects.common.elasticLogger)
    implementation(projects.common.sentryLogger)
}
