object Dependencies {

    object Versions {
        val okhttp = "4.7.2"
        val sentry = "1.7.23"
        val retrofit = "2.9.0"
        val androidXTest = "1.2.0"
        val junit5 = "5.6.0"
        val junit5Platform = "1.6.0"
        val androidX = "1.0.0"
        val espresso = "3.2.0"
        val mockito = "3.3.3"
        val detekt = "1.10.0"
        val coroutines = "1.3.7"
        val androidGradlePlugin = System.getProperty("androidGradlePluginVersion")
    }

    val kotlinXCli = "org.jetbrains.kotlinx:kotlinx-cli:0.2.1"
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect"
    val kotlinHtml = "org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1"
    val kotlinCompilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable"
    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    val okio = "com.squareup.okio:okio:2.7.0"
    val funktionaleTry = "org.funktionale:funktionale-try:1.2"
    val gson = "com.google.code.gson:gson:2.8.5"
    val kotson = "com.github.salomonbrys.kotson:kotson:2.5.0"
    val sentry = "io.sentry:sentry:${Versions.sentry}"
    val sentryAndroid = "io.sentry:sentry-android:${Versions.sentry}"

    //https://github.com/JetBrains/teamcity-rest-client
    val teamcityClient = "org.jetbrains.teamcity:teamcity-rest-client:1.6.2"
    val googlePublish = "com.google.apis:google-api-services-androidpublisher:v3-rev86-1.25.0"
    val bcel = "org.apache.bcel:bcel:6.3.1"
    val slackClient = "com.github.seratch:jslack-api-client:3.4.1"
    val rxJava = "io.reactivex:rxjava:1.3.8"
    val statsd = "com.timgroup:java-statsd-client:3.1.0"

    // We use this client due to better API
    // It supports all features we need and actively maintained
    val kubernetesClient = "io.fabric8:kubernetes-client:4.9.0"
    // We use the official kubernetes client only for missing features
    val officialKubernetesClient = "io.kubernetes:client-java:8.0.0"
    val googleAuthLibrary = "com.google.auth:google-auth-library-oauth2-http:0.10.0"
    val kubernetesDsl = "com.fkorotkov:kubernetes-dsl:2.7.1"
    val dexlib = "org.smali:dexlib2:2.3"
    val commonsText = "org.apache.commons:commons-text:1.6"
    val commonsIo = "commons-io:commons-io:2.7"
    val commonsLang = "org.apache.commons:commons-lang3:3.8.1"
    val antPattern = "io.github.azagniotov:ant-style-path-matcher:1.0.0"
    val dockerClient = "de.gesellix:docker-client:2019-11-26T12-39-35"
    val asm = "org.ow2.asm:asm:7.1"
    val detektParser = "io.gitlab.arturbosch.detekt:detekt-parser:${Versions.detekt}"
    val detektCli = "io.gitlab.arturbosch.detekt:detekt-cli:${Versions.detekt}"

    // https://r8.googlesource.com/r8/
    // 1.6.x <-> AGP 3.6.x
    // 2.0.x <-> AGP 4.0.x
    // 2.1.x <-> AGP 4.1.x
    val r8 = "com.android.tools:r8:2.0.94"
    val proguardRetrace = "net.sf.proguard:proguard-retrace:6.2.2"
    val playServicesMaps = "com.google.android.gms:play-services-maps:17.0.0"
    val appcompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    val recyclerView = "androidx.recyclerview:recyclerview:1.1.0"
    val material = "com.google.android.material:material:${Versions.androidX}"
    val androidAnnotations = "androidx.annotation:annotation:1.1.0"
    val freeReflection = "me.weishu:free_reflection:2.2.0"

    object gradle {
        val androidPlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
        val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin"
        object avito {
            val kotlinDslSupport = "com.avito.android:kotlin-dsl-support"
            val utils = "com.avito.android:utils"
            val buildEnvironment = "com.avito.android:build-environment"
        }
    }

    object androidTest {
        val runner = "androidx.test:runner:${Versions.androidXTest}"
        val orchestrator = "androidx.test:orchestrator:${Versions.androidXTest}"
        val ddmlib = "com.android.tools.ddms:ddmlib:26.2.0"
        val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"
        val core = "androidx.test:core:${Versions.androidXTest}"
        val rules = "androidx.test:rules:${Versions.androidXTest}"
        val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
        val espressoWeb = "androidx.test.espresso:espresso-web:${Versions.espresso}"
        val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"
        val espressoDescendantActions = "com.forkingcode.espresso.contrib:espresso-descendant-actions:1.4.0"
        val kaspresso = "com.kaspersky.android-components:kaspresso:1.1.0"
    }

    object test {
        val okhttpMockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
        val okhttpMock = "com.github.gmazzo:okhttp-mock:1.2.1"
        val junit = "junit:junit:4.13"
        val truth = "com.google.truth:truth:1.0"
        val kotlinCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.5"
        val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
        val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        val mockitoJUnitJupiter = "org.mockito:mockito-junit-jupiter:${Versions.mockito}"
        val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
        val jsonPathAssert = "com.jayway.jsonpath:json-path-assert:2.4.0"
        val kotlinTest = "io.kotlintest:kotlintest:2.0.7"
        val kotlinTestJUnit = "org.jetbrains.kotlin:kotlin-test-junit"
        val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
        val hamcrestLib = "org.hamcrest:hamcrest-library:1.3"
        val junitPlatformRunner = "org.junit.platform:junit-platform-runner:${Versions.junit5Platform}"
        val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:${Versions.junit5Platform}"
        val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
        val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }
}
