package com.avito.deeplink_generator.utils

import com.avito.deeplink_generator.model.Deeplink
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.xmlunit.matchers.EvaluateXPathMatcher
import java.io.File

const val ACTIVITY_MANIFEST_PATH = "//manifest/application/activity"
const val INTENT_FILTER_MANIFEST_PATH = "$ACTIVITY_MANIFEST_PATH/intent-filter"
const val DATA_MANIFEST_PATH = "$INTENT_FILTER_MANIFEST_PATH/data"

fun getGeneratedManifestLibFile(libDir: File) =
    File(
        libDir,
        "/build/intermediates/merged_manifest/release/mergeReleasePublicDeeplinkManifest/AndroidManifest.xml"
    )

fun getGeneratedManifestAppFile(appDir: File) =
    File(
        appDir,
        "/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml"
    )

fun getGeneratedFilteredManifestAppFile(appDir: File) =
    File(
        appDir,
        "/build/intermediates/merged_manifest/release/filterReleasePublicDeeplinkManifest/AndroidManifest.xml"
    )

fun validateManifest(
    manifestProjectDir: File,
    isForLib: Boolean = true,
    withFilter: Boolean = false,
    manifestValidator: (manifest: File) -> Unit
) {
    val manifest = if (isForLib) {
        getGeneratedManifestLibFile(manifestProjectDir)
    } else if (withFilter) {
        getGeneratedFilteredManifestAppFile(manifestProjectDir)
    } else {
        getGeneratedManifestAppFile(manifestProjectDir)
    }
    assertThat(manifest.exists()).isTrue()
    manifestValidator.invoke(manifest)
}

/**
 * Manifest merger 31.13+ (AGP 8.13) fails the merge on an input manifest containing <uses-sdk>
 * unless [com.android.manifmerger.ManifestMerger2.Invoker.Feature.USES_SDK_IN_MANIFEST_LENIENT_HANDLING]
 * is enabled by the merging task.
 */
fun File.writeManifestWithUsesSdk() {
    File(this, "src/main/AndroidManifest.xml").writeText(
        """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <uses-sdk android:minSdkVersion="21" />
        </manifest>
        """.trimIndent()
    )
}

fun libModule(
    name: String = "lib",
    buildScriptLinks: List<String> = listOf("1/feed"),
    codeLinks: List<String> = listOf("1/feed ru.avito"),
    mutator: File.() -> Unit = {},
) = AndroidLibModule(
    name = name,
    enableKotlinAndroidPlugin = false,
    plugins = plugins { id("com.avito.android.deeplink-generator") },
    mutator = mutator,
    imports = listOf(
        "import com.avito.deeplink_generator.MergePublicDeeplinkManifestTask",
        "import java.io.File",
        "import org.gradle.api.tasks.compile.JavaCompile",
        "import com.android.build.gradle.BaseExtension",
        "import com.avito.kotlin.dsl.typedNamedOrNull",
    ),
    buildGradleExtra = """
                        deeplinkGenerator {
                           activityIntentFilterClass.set("com.avito.deeplink_generator.SomeActivity")
                           defaultScheme.set("ru.avito")
                           
                           publicDeeplinks(
                                ${buildScriptLinks.joinToString { "\"$it\"" }}
                           )
                        }
                        
                       android.libraryVariants.configureEach {
                            val variant = this
                            val variantName = variant.name.capitalize()
                            val generateDeeplinksFromCode = 
                                tasks.register("generate%sDeeplinksFromCode".format(variantName)) {
                                    doLast { 
                                        val file = File(project.buildDir, "public_links.tmp")
                                        file.parentFile.mkdirs()
                                        file.createNewFile()
                                        file.writer().use {
                                            it.write(
                                                listOf<String>(${codeLinks.joinToString { "\"$it\"" }})
                                                .joinToString(separator = "\n")
                                            )
                                        }
                                    }
                            } 
                            tasks.typedNamedOrNull<MergePublicDeeplinkManifestTask>(
                                MergePublicDeeplinkManifestTask.taskName(variantName)
                            )?.let { provider ->
                                provider.configure {
                                    dependsOn(generateDeeplinksFromCode)
                                    publicDeeplinksFromCode.set(File(project.buildDir, "public_links.tmp"))
                                }
                            }
                       } 

                    """.trimIndent(),
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

fun File.assertDeeplinkCount(count: Int) {
    val manifest = this

    assertThat(manifest, hasPath("count($DATA_MANIFEST_PATH)", IsEqual(count.toString())))
}

fun File.assertContainsDeeplink(deeplink: Deeplink, filterPosition: Int = 1, dataPosition: Int = 1) {
    val manifest = this
    val dataManifestPath =
        "$INTENT_FILTER_MANIFEST_PATH[position()=$filterPosition]/data[position()=$dataPosition]"
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
