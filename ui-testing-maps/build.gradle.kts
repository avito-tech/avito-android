plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
}

//todo cleaner way to get these properties
val buildTools = requireNotNull(project.properties["buildToolsVersion"]).toString()
val compileSdk = requireNotNull(project.properties["compileSdkVersion"]).toString().toInt()
val targetSdk = requireNotNull(project.properties["targetSdkVersion"]).toString()
val minSdk = requireNotNull(project.properties["minSdkVersion"]).toString()

android {
    buildToolsVersion(buildTools)
    compileSdkVersion(compileSdk)

    defaultConfig {
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)
    }
}

dependencies {
    api(project(":ui-testing-core"))
    api("com.google.android.gms:play-services-maps:17.0.0")
}
