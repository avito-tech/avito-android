package com.avito.deeplink_generator

import com.avito.deeplink_generator.model.Deeplink
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.xmlunit.matchers.EvaluateXPathMatcher
import java.io.File

internal class MergeDeeplinkManifestTest {

    @Test
    fun `assemble single library two times - manifest task is up to date`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule()
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":feed:assembleRelease")
            .assertThat()
            .taskWithOutcome(":feed:mergeReleasePublicDeeplinkManifest", TaskOutcome.SUCCESS)
        gradlew(projectDir, ":feed:assembleRelease")
            .assertThat()
            .taskWithOutcome(":feed:mergeReleasePublicDeeplinkManifest", TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `assemble single library without deeplinks - manifest with deeplinks is not generated`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(deeplinks = emptyArray())
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":feed:assembleRelease").assertThat()
            .buildSuccessful()
            .tasksShouldNotBeTriggered(":feed:mergeReleasePublicDeeplinkManifest")

        val manifest = getGeneratedManifestLibFile(File(projectDir, "feed"))
        assertThat(manifest.exists()).isFalse()
    }

    @Test
    fun `assemble single library - manifest generated correctly`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule()
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":feed:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "feed"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter()
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"))
            }
        )
    }

    @Test
    fun `assemble single library with different hosts - manifest divided by intent filter blocks`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    name = "feed",
                    "1/feed",
                    "2/feed"
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":feed:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "feed"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter(count = 2)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"), position = 1)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "2", "/feed"), position = 2)
            }
        )
    }

    @Test
    fun `assemble app with several libraries - app manifest contains merged information about links`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    dependencies = setOf(
                        project(":feed"),
                        project(":profile")
                    )
                ),
                libModule("feed", "1/feed"),
                libModule("profile", "1/profile")
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":app:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "app"),
            isForLib = false,
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"), position = 1)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/profile"), position = 2)
            }
        )
    }

    private companion object {

        const val ACTIVITY_MANIFEST_PATH = "//manifest/application/activity"
        const val INTENT_FILTER_MANIFEST_PATH = "$ACTIVITY_MANIFEST_PATH/intent-filter"

        fun getGeneratedManifestLibFile(libDir: File) =
            File(
                libDir,
                "/build/intermediates/merged_manifest/release/mergeReleasePublicDeeplinkManifest/AndroidManifest.xml"
            )

        fun getGeneratedManifestAppFile(appDir: File) =
            File(
                appDir,
                "/build/intermediates/merged_manifest/release/AndroidManifest.xml"
            )

        fun validateManifest(
            manifestProjectDir: File,
            isForLib: Boolean = true,
            manifestValidator: (manifest: File) -> Unit
        ) {
            val manifest = if (isForLib) {
                getGeneratedManifestLibFile(manifestProjectDir)
            } else {
                getGeneratedManifestAppFile(manifestProjectDir)
            }
            assertThat(manifest.exists()).isTrue()
            manifestValidator.invoke(manifest)
        }

        fun libModule(name: String = "feed", vararg deeplinks: String = arrayOf("1/feed")) = AndroidLibModule(
            name = name,
            enableKotlinAndroidPlugin = false,
            plugins = plugins { id("com.avito.android.deeplink-generator") },
            buildGradleExtra = """
                        deeplinkGenerator {
                           activityIntentFilterClass.set("com.avito.deeplink_generator.SomeActivity")
                           defaultScheme.set("ru.avito")
                           
                           publicDeeplinks(
                              ${deeplinks.joinToString { "\"$it\"" }}
                           ) 
                        }
                    """.trimMargin(),
            useKts = true
        )

        fun File.assertContainsActivity(activityName: String) {
            val manifest = this
            assertThat(manifest, hasPath("$ACTIVITY_MANIFEST_PATH/@android:name", IsEqual(activityName)))
        }

        fun File.assertContainsIntentFilter(count: Int = 1) {
            val manifest = this
            assertThat(manifest, hasPath("count($INTENT_FILTER_MANIFEST_PATH)", IsEqual(count.toString())))
        }

        fun File.assertContainsDeeplink(deeplink: Deeplink, position: Int = 1) {
            val manifest = this
            val dataManifestPath = "$INTENT_FILTER_MANIFEST_PATH[position()=$position]/data"
            val dataVariable = "$dataManifestPath/@android:%s"

            assertThat(manifest, hasPath(dataVariable.format("scheme"), IsEqual(deeplink.scheme)))
            assertThat(manifest, hasPath(dataVariable.format("pathPattern"), IsEqual(deeplink.path)))
            assertThat(manifest, hasPath(dataVariable.format("host"), IsEqual(deeplink.host)))
        }

        private fun hasPath(xPath: String, matcher: Matcher<String>) =
            EvaluateXPathMatcher.hasXPath(xPath, matcher)
                .withNamespaceContext(
                    mapOf("android" to "http://schemas.android.com/apk/res/android")
                )
    }
}
