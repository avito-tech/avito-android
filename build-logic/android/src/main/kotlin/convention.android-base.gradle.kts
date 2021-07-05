import com.android.build.gradle.BaseExtension

configure<BaseExtension> {
    sourceSets {
        named("main").configure { java.srcDir("src/main/kotlin") }
        named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
        named("test").configure { java.srcDir("src/test/kotlin") }
    }

    // workaround for https://github.com/gradle/gradle/issues/15383
    if (project.name != "gradle-kotlin-dsl-accessors") {
        val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

        buildToolsVersion(libs.versions.buildTools.get())
        compileSdkVersion(libs.versions.compileSdk.get().toInt())

        defaultConfig {
            minSdkVersion(libs.versions.minSdk.get().toInt())
            targetSdkVersion(libs.versions.targetSdk.get().toInt())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
