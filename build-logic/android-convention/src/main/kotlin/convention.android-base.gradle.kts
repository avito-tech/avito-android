import com.android.build.gradle.BaseExtension

plugins {
    id("convention.libraries")
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

    @Suppress("UnstableApiUsage")
    with(buildFeatures) {
        aidl = false
        compose = false
        buildConfig = false
        prefab = false
        renderScript = false
        resValues = false
        shaders = false
        viewBinding = false
    }
}
