@file:Suppress("SpellCheckingInspection")

object Dependencies {

    object Versions {
        const val okhttp = "4.9.0"
        const val sentry = "1.7.23"
        const val retrofit = "2.9.0"
        const val androidXTest = "1.2.0"
        const val junit5 = "5.6.0"
        const val junit5Platform = "1.6.0"
        const val androidX = "1.0.0"
        const val espresso = "3.2.0"
        const val mockito = "3.3.3"
        const val detekt = "1.10.0"
        const val coroutines = "1.3.7"
    }

    const val kotlinXCli = "org.jetbrains.kotlinx:kotlinx-cli:0.2.1"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect"
    const val kotlinHtml = "org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2"
    const val kotlinCompilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val retrofitConverterScalars = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val okio = "com.squareup.okio:okio:2.7.0"
    const val funktionaleTry = "org.funktionale:funktionale-try:1.2"
    const val gson = "com.google.code.gson:gson:2.8.5"
    const val kotson = "com.github.salomonbrys.kotson:kotson:2.5.0"
    const val sentry = "io.sentry:sentry:${Versions.sentry}"
    const val sentryAndroid = "io.sentry:sentry-android:${Versions.sentry}"
    const val slf4jApi = "org.slf4j:slf4j-api:1.7.28"

    //https://github.com/JetBrains/teamcity-rest-client
    const val teamcityClient = "org.jetbrains.teamcity:teamcity-rest-client:1.6.2"
    const val googlePublish = "com.google.apis:google-api-services-androidpublisher:v3-rev86-1.25.0"
    const val bcel = "org.apache.bcel:bcel:6.3.1"
    const val slackClient = "com.github.seratch:jslack-api-client:3.4.1"
    const val rxJava = "io.reactivex:rxjava:1.3.8"
    const val statsd = "com.timgroup:java-statsd-client:3.1.0"

    // We use this client due to better API
    // It supports all features we need and actively maintained
    const val kubernetesClient = "io.fabric8:kubernetes-client:4.9.0"

    // We use the official kubernetes client only for missing features
    const val officialKubernetesClient = "io.kubernetes:client-java:8.0.0"
    const val googleAuthLibrary = "com.google.auth:google-auth-library-oauth2-http:0.10.0"
    const val kubernetesDsl = "com.fkorotkov:kubernetes-dsl:2.7.1"
    const val dexlib = "org.smali:dexlib2:2.3"
    const val commonsText = "org.apache.commons:commons-text:1.6"
    const val commonsIo = "commons-io:commons-io:2.7"
    const val commonsLang = "org.apache.commons:commons-lang3:3.8.1"
    const val antPattern = "io.github.azagniotov:ant-style-path-matcher:1.0.0"
    const val dockerClient = "de.gesellix:docker-client:2019-11-26T12-39-35"

    // https://r8.googlesource.com/r8/
    // 1.6.x <-> AGP 3.6.x
    // 2.0.x <-> AGP 4.0.x
    // 2.1.x <-> AGP 4.1.x
    const val r8 = "com.android.tools:r8:2.1.80"
    const val proguardRetrace = "net.sf.proguard:proguard-retrace:6.2.2"
    const val playServicesMaps = "com.google.android.gms:play-services-maps:17.0.0"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.1.0"
    const val material = "com.google.android.material:material:${Versions.androidX}"
    const val androidAnnotations = "androidx.annotation:annotation:1.1.0"
    const val freeReflection = "me.weishu:free_reflection:2.2.0"

    object Gradle {
        const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin"

        object Avito {
            const val kotlinDslSupport = "com.avito.android:kotlin-dsl-support"
            const val utils = "com.avito.android:utils"
            const val buildEnvironment = "com.avito.android:build-environment"
        }
    }

    object AndroidTest {
        const val runner = "androidx.test:runner:${Versions.androidXTest}"
        const val orchestrator = "androidx.test:orchestrator:${Versions.androidXTest}"
        const val ddmlib = "com.android.tools.ddms:ddmlib:26.2.0"
        const val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"
        const val core = "androidx.test:core:${Versions.androidXTest}"
        const val rules = "androidx.test:rules:${Versions.androidXTest}"
        const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
        const val espressoWeb = "androidx.test.espresso:espresso-web:${Versions.espresso}"
        const val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"
        const val espressoDescendantActions = "com.forkingcode.espresso.contrib:espresso-descendant-actions:1.4.0"
        const val kaspresso = "com.kaspersky.android-components:kaspresso:1.1.0"
        const val rxIlder = "com.squareup.rx.idler:rx3-idler:0.11.0"
    }

    object Test {
        const val okhttpMockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
        const val okhttpMock = "com.github.gmazzo:okhttp-mock:1.2.1"
        const val junit = "junit:junit:4.13"
        const val truth = "com.google.truth:truth:1.0"
        const val kotlinCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.5"
        const val kotlinPoet = "com.squareup:kotlinpoet:1.7.2"
        const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        const val mockitoJUnitJupiter = "org.mockito:mockito-junit-jupiter:${Versions.mockito}"
        const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
        const val jsonPathAssert = "com.jayway.jsonpath:json-path-assert:2.4.0"
        const val kotlinTest = "io.kotlintest:kotlintest:2.0.7"
        const val kotlinTestJUnit = "org.jetbrains.kotlin:kotlin-test-junit"
        const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
        const val hamcrestLib = "org.hamcrest:hamcrest-library:1.3"
        const val junitPlatformRunner = "org.junit.platform:junit-platform-runner:${Versions.junit5Platform}"
        const val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:${Versions.junit5Platform}"
        const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }
}
