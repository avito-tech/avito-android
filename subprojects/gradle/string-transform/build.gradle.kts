plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.androidGradle)
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))

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
