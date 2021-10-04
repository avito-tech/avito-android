import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.VerifyLibraryResourcesTask
import com.avito.android.withVersionCatalog

configure<BaseExtension> {
    sourceSets {
        named("main").configure { java.srcDir("src/main/kotlin") }
        named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
        named("test").configure { java.srcDir("src/test/kotlin") }
    }

    project.withVersionCatalog { libs ->
        buildToolsVersion(libs.versions.buildTools.get())
        compileSdkVersion(libs.versions.compileSdk.get().toInt())

        defaultConfig {
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
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
        isCheckReleaseBuilds = false
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

tasks.withType<VerifyLibraryResourcesTask>().configureEach {
    // todo fix and enable MBS-11914
    onlyIf { false }
}
