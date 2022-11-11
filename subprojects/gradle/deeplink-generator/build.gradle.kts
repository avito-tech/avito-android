plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.android)

    implementation(projects.subprojects.common.result)
    implementation(libs.androidToolsCommon)
    implementation(libs.androidGradle)
    gradleTestImplementation(libs.xmlUnit.core)
    gradleTestImplementation(libs.xmlUnit.matchers)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("deeplinkGeneratorPlugin") {
            id = "com.avito.android.deeplink-generator"
            implementationClass = "com.avito.deeplink_generator.DeeplinkGeneratorPlugin"
            displayName = "Automatic generation of AndroidManifest entries for public deeplinks"
        }
    }
}
