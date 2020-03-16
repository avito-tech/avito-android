object Dependencies {

    object Versions {
        val okhttp = "3.14.6"
        val sentry = "1.7.23"
        val retrofit = "2.6.2"
        val kotlin = "1.3.61"
        val androidXTest = "1.2.0"
        val junit5 = "5.6.0"
        val junit5Platform = "1.6.0"
        val androidX = "1.0.0"
        val espresso = "3.2.0"
    }

    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val kotlinHtml = "org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.9"
    val kotlinCompilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:${Versions.kotlin}"
    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.2"
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    val okio = "com.squareup.okio:okio:2.1.0"
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
    val kubernetesClient = "io.fabric8:kubernetes-client:4.6.3"
    val kubernetesDsl = "com.fkorotkov:kubernetes-dsl:1.2.1"
    val dexlib = "org.smali:dexlib2:2.3"
    val commonsText = "org.apache.commons:commons-text:1.6"
    val antPattern = "io.github.azagniotov:ant-style-path-matcher:1.0.0"
    val dockerClient = "de.gesellix:docker-client:2019-11-26T12-39-35"
    val asm = "org.ow2.asm:asm:7.1"

    //https://r8.googlesource.com/r8/
    val r8 = "com.android.tools:r8:1.6.58"
    val playServicesMaps = "com.google.android.gms:play-services-maps:17.0.0"
    val appcompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    val recyclerView = "androidx.recyclerview:recyclerview:${Versions.androidX}"
    val material = "com.google.android.material:material:${Versions.androidX}"
    val androidAnnotations = "androidx.annotation:annotation:1.1.0"
    val freeReflection = "me.weishu:free_reflection:2.2.0"

    object gradle {
        val androidPlugin = "com.android.tools.build:gradle:3.5.3"
        val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        object avito {
            val kotlinDslSupport = "com.avito.android:kotlin-dsl-support"
        }
    }

    object androidTest {
        val runner = "androidx.test:runner:${Versions.androidXTest}"
        val ddmlib = "com.android.tools.ddms:ddmlib:26.2.0"
        val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"
        val core = "androidx.test:core:${Versions.androidXTest}"
        val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
        val espressoWeb = "androidx.test.espresso:espresso-web:${Versions.espresso}"
        val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"
        val espressoDescendantActions = "com.forkingcode.espresso.contrib:espresso-descendant-actions:1.4.0"
    }

    object test {
        val okhttpMockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
        val okhttpMock = "com.github.gmazzo:okhttp-mock:1.2.1"
        val junit = "junit:junit:4.13"
        val truth = "com.google.truth:truth:1.0"
        val kotlinCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.5"
        val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
        val mockitoKotlin = "com.nhaarman:mockito-kotlin:1.5.0"
        val mockitoJUnitJupiter = "org.mockito:mockito-junit-jupiter:2.23.4"
        val mockitoKotlin2 = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0"
        val mockitoCore = "org.mockito:mockito-core:2.18.3"
        val jsonPathAssert = "com.jayway.jsonpath:json-path-assert:2.4.0"
        val kotlinTest = "io.kotlintest:kotlintest:2.0.7"
        val kotlinTestJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
        val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5}"
        val hamcrestLib = "org.hamcrest:hamcrest-library:1.3"
        val junitPlatformRunner = "org.junit.platform:junit-platform-runner:${Versions.junit5Platform}"
        val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:${Versions.junit5Platform}"
        val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}"
    }
}
