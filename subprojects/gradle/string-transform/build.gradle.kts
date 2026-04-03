plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.androidGradle)
    implementation("com.android.tools.build:aapt2-proto:8.8.2-12006047")
    implementation("com.google.protobuf:protobuf-java:3.22.3")
    implementation("org.smali:dexlib2:2.3")
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))

    testFixturesImplementation("com.android.tools.build:aapt2-proto:8.8.2-12006047")
    testFixturesImplementation("com.google.protobuf:protobuf-java:3.22.3")
    testFixturesImplementation("org.smali:dexlib2:2.3")

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
