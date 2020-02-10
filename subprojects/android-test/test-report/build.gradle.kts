plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
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
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:android-test:test-annotations"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation("com.squareup.okio:okio:$okioVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion") //todo remove reflect call
    implementation("io.sentry:sentry-android:$sentryVersion") //todo use common:sentry

    testImplementation(project(":subprojects:android-test:mockito-utils"))
    testImplementation(project(":subprojects:android-test:junit-utils"))
    testImplementation("com.github.gmazzo:okhttp-mock:$okhttpMockVersion")
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("com.google.truth:truth:$truthVersion")
    testImplementation("com.jayway.jsonpath:json-path-assert:$jsonPathVersion")
}
