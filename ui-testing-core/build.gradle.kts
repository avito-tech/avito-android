plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
}

val androidXTestVersion: String by project
val espressoVersion: String by project
val androidXVersion: String by project
val kotlinVersion: String by project
val hamcrestVersion: String by project
val junitVersion: String by project

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
    api("androidx.test:core:$androidXTestVersion")
    api("androidx.test.espresso:espresso-core:$espressoVersion")
    api("androidx.test.espresso:espresso-web:$espressoVersion")
    api("androidx.test.espresso:espresso-intents:$espressoVersion")
    api("androidx.test.uiautomator:uiautomator:2.2.0")

    api("com.forkingcode.espresso.contrib:espresso-descendant-actions:1.4.0")

    api("androidx.appcompat:appcompat:$androidXVersion")
    api("androidx.recyclerview:recyclerview:$androidXVersion")
    api("com.google.android.material:material:$androidXVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    implementation("junit:junit:$junitVersion")
    implementation("me.weishu:free_reflection:2.2.0")
}
