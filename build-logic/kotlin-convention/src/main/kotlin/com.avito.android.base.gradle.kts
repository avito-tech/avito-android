import com.android.build.gradle.BaseExtension

plugins {
    id("com.avito.android.libraries")
}

configure<BaseExtension> {
    sourceSets {
        named("main").configure { java.srcDir("src/main/kotlin") }
        named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
        named("test").configure { java.srcDir("src/test/kotlin") }
    }

    buildToolsVersion(libs.buildToolsVersion)
    compileSdkVersion(libs.compileSdkVersion)

    compileOptions {
        sourceCompatibility = libs.javaVersion
        targetCompatibility = libs.javaVersion
    }

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)
    }

    lintOptions {
        isAbortOnError = false
        isWarningsAsErrors = true
        textReport = true
        isQuiet = true
    }
}
