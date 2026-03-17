plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.androidGradle)
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("stringTransform") {
            id = "com.avito.android.string-transform"
            implementationClass = "com.avito.android.string_transform.StringTransformPlugin"
            displayName = "String Transform Plugin"
        }
    }
}
