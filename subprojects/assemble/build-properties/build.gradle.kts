plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:git"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:gradle:git"))
}

gradlePlugin {
    plugins {
        create("buildProperties") {
            id = "com.avito.android.build-properties"
            implementationClass = "com.avito.android.info.BuildPropertiesPlugin"
            displayName = "Build properties"
        }
    }
}
