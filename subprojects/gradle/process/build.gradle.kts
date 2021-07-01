plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.common.result)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(projects.logger.logger)
}
