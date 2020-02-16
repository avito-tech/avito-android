plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
}

val okhttpVersion: String by project
val truthVersion: String by project
val androidXTestVersion: String by project
val kotlinPoetVersion: String by project
val kotlinCompileTestingVersion: String by project
val gsonVersion: String by project

dependencies {
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:android-test:junit-utils"))
    implementation(project(":subprojects:android-test:test-report"))
    implementation(project(":subprojects:android-test:test-annotations"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:ui-testing-maps"))
    implementation(project(":subprojects:android-test:mockito-utils"))
    implementation("androidx.test:runner:$androidXTestVersion")
    implementation("com.google.truth:truth:$truthVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    testImplementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:$kotlinCompileTestingVersion")
}
