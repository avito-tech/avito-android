plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.androidGradle)
    implementation(libs.aapt2Proto)
    implementation(libs.protobufJava)
    implementation(libs.dexlib)
    implementation(libs.kotlinMetadataJvm)
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))

    testFixturesImplementation(libs.aapt2Proto)
    testFixturesImplementation(libs.protobufJava)
    testFixturesImplementation(libs.dexlib)
    testFixturesImplementation(libs.kotlinMetadataJvm)

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
