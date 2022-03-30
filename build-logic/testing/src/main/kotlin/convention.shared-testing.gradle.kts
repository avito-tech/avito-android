import com.android.build.gradle.BaseExtension
import com.avito.android.withVersionCatalog
import org.gradle.kotlin.dsl.configure

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
        @Suppress("UnstableApiUsage")
        unitTests {
            isIncludeAndroidResources = true

            all {
                val args = mapOf(
                    "testResultsDir" to buildDir.resolve("report-viewer").path,
                    "reportApiUrl" to stringProperty("avito.report.url", "http://stub"),
                    "reportViewerUrl" to stringProperty("avito.report.viewerUrl", "http://stub"),
                    "fileStorageUrl" to stringProperty("avito.fileStorage.url", "http://stub"),

                    // TODO: apply shared-testing plugin
                    "runId" to "local",
                    "robolectric.logging.enabled" to "true",
                    "robolectric.alwaysIncludeVariantMarkersInTestName" to "true",
                )

                it.systemProperties.putAll(args)
            }
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

fun stringProperty(name: String, default: String): String {
    if (hasProperty(name)) {
        return property(name)?.toString() ?: default
    }
    return default
}
