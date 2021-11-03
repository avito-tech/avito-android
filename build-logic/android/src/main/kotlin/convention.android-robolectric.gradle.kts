import com.android.build.gradle.BaseExtension
import com.avito.android.withVersionCatalog

plugins {
    id("convention.android-base")
}

configure<BaseExtension> {
    val sharedTestSourcesFolder = "src/sharedTest/kotlin"
    val sharedTestResourcesFolder = "src/sharedTest/resources"

    sourceSets {
        named("test").configure {
            java.srcDir(sharedTestSourcesFolder)
            resources.srcDir(sharedTestResourcesFolder)
        }
        named("androidTest").configure {
            java.srcDir(sharedTestSourcesFolder)
            resources.srcDir(sharedTestResourcesFolder)
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

project.withVersionCatalog { libs ->
    dependencies {
        add("testImplementation", libs.robolectric)
        add("testImplementation", libs.androidXTestExtJunit)
        add("androidTestImplementation", libs.androidXTestExtJunit)
        add("testRuntimeOnly", libs.junitVintageEngine)
    }
}
