import com.android.build.gradle.BaseExtension
import com.avito.android.withVersionCatalog
import gradle.kotlin.dsl.accessors._952aaa10a15315a4fb47c4ae90ef5b7c.testImplementation

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
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

project.withVersionCatalog { libs ->
    dependencies {
        testImplementation(libs.robolectric)
        sharedTestImplementation(libs.androidXTestExtJunit)
    }
}
