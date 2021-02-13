import com.android.build.gradle.BaseExtension
import org.gradle.kotlin.dsl.configure

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
        minSdkVersion(libs.minSdkVersion)
        targetSdkVersion(libs.targetSdkVersion)
    }

    lintOptions {
        isAbortOnError = false
        isWarningsAsErrors = true
        textReport = true
        isQuiet = true
    }
}
