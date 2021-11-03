plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.subprojects.logger.logger)
    api(projects.subprojects.common.time)

    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.logger.sentryLogger)

    testImplementation(libs.mockitoJUnitJupiter)
}

gradlePlugin {
    plugins {
        create("logger") {
            id = "com.avito.android.gradle-logger"
            implementationClass = "com.avito.logger.GradleLoggerPlugin"
            displayName = "Gradle Logger"
        }
    }
}
