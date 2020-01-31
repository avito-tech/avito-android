plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("digital.wup.android-maven-publish")
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

    lintOptions {
        isAbortOnError = false
        isWarningsAsErrors = true
        textReport = true
    }
}

val kotlinVersion: String by project
val okhttpVersion: String by project
val truthVersion: String by project
val kotsonVersion: String by project
val funktionaleVersion: String by project
val androidXTestVersion: String by project
val retrofitVersion: String by project
val androidXUiautomatorVersion: String by project
val timberVersion: String by project
val sentryVersion: String by project
val okhttpMockVersion: String by project
val mockitoKotlinVersion: String by project
val jsonPathVersion: String by project
val okioVersion: String by project

dependencies {
    implementation(project(":subprojects:android-test:test-annotations"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation("com.squareup.okio:okio:$okioVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("org.funktionale:funktionale-either:$funktionaleVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("androidx.test:runner:$androidXTestVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("androidx.test.uiautomator:uiautomator:$androidXUiautomatorVersion")
    implementation("com.jakewharton.timber:timber:$timberVersion")
    implementation("io.sentry:sentry-android:$sentryVersion")

    testImplementation(project(":subprojects:android-test:mockito-utils"))
    testImplementation(project(":subprojects:android-test:junit-utils"))
    testImplementation("com.github.gmazzo:okhttp-mock:$okhttpMockVersion")
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("com.google.truth:truth:$truthVersion")
    testImplementation("com.jayway.jsonpath:json-path-assert:$jsonPathVersion")
}
