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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

val okhttpVersion: String by project
val truthVersion: String by project
val funktionaleVersion: String by project
val androidXTestVersion: String by project
val retrofitVersion: String by project
val androidXUiautomatorVersion: String by project
val timberVersion: String by project
val mockitoKotlinVersion: String by project
val bouncyCastleVersion: String by project
val arrowVersion: String by project
val kotlinPoetVersion: String by project
val kotlinCompileTestingVersion: String by project

dependencies {
    implementation(project(":junit-utils"))
    implementation(project(":mockito-utils"))
    implementation(project(":test-report"))
    implementation(project(":test-annotations"))
    implementation("androidx.test:runner:$androidXTestVersion")
    implementation("com.jakewharton.timber:timber:$timberVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.google.truth:truth:$truthVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    implementation("com.squareup.okhttp3:okhttp-tls:$okhttpVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:$bouncyCastleVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("org.funktionale:funktionale-either:$funktionaleVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("androidx.test.uiautomator:uiautomator:$androidXUiautomatorVersion")
    implementation(project(":sentry"))
    implementation(project(":okhttp"))
    implementation(project(":statsd"))
    implementation(project(":report-viewer"))
    implementation(project(":ui-testing-core"))
    implementation(project(":ui-testing-maps"))
    implementation(project(":mockito-utils"))
    implementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:$kotlinCompileTestingVersion")
}
