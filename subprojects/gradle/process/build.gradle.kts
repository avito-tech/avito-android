plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.result)

    // todo used only for Commandline.translateCommandline(source)
    implementation(gradleApi())
    implementation(projects.subprojects.logger.logger)
}
