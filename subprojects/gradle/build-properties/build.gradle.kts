plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.android)
    implementation(projects.gradle.preBuild)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.gradleExtensions)

    gradleTestImplementation(projects.gradle.testProject)
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
