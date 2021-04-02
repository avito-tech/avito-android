plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.kotlinStdlib)

    implementation(project(":gradle:android"))
    implementation(project(":gradle:pre-build"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))

    gradleTestImplementation(project(":gradle:test-project"))
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
