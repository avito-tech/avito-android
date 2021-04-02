import com.android.build.gradle.BaseExtension

configure<BaseExtension> {
    sourceSets {
        named("main").configure { java.srcDir("src/main/kotlin") }
        named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
        named("test").configure { java.srcDir("src/test/kotlin") }
    }

    buildToolsVersion("29.0.3")
    compileSdkVersion(29)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
