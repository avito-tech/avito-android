plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:time"))

    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:logger:elastic-logger"))
    implementation(project(":subprojects:logger:sentry-logger"))

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
