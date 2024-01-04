plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:android"))

    implementation(project(":subprojects:common:result"))
    implementation(libs.androidToolsCommon)
    implementation(libs.androidGradle)
    gradleTestImplementation(libs.xmlUnit.core)
    gradleTestImplementation(libs.xmlUnit.matchers)
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
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
