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

                /**
                 * shared tests was failing due to "OOM loading robolectric.jar via nativeLoad"
                 * analysis has shown that real memory consumption comes close to 1gb
                 * default Xmx is `512Mb`, which was not enough and was leading to flaky runs
                 */
                it.maxHeapSize = "1024m"

                /**
                 * uncomment it and use with
                 * `while ./gradlew :subprojects:android-test:ui-testing-core-app:testDebugUnitTest; do :; done`
                 * to run test suites in loop until it fails if you need to make sure that config is stable enough
                 */
                 // it.outputs.upToDateWhen { false }
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
