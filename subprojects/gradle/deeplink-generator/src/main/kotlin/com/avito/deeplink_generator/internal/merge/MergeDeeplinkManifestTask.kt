package com.avito.deeplink_generator.internal.merge

import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.utils.NullLogger
import com.avito.capitalize
import com.avito.deeplink_generator.model.Deeplink
import com.google.common.base.Charsets
import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Merges default library manifest with generated manifest with intent filters
 * to open public deeplinks.
 */
@CacheableTask
internal abstract class MergeDeeplinkManifestTask : DefaultTask() {

    /**
     * Collection of public deeplinks that can launch application from outside.
     */
    @get:Input
    abstract val publicLinks: SetProperty<Deeplink>

    /**
     * Fully qualified class name of an Activity responsible for handling public deeplinks.
     */
    @get:Input
    abstract val activityIntentFilterClass: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputManifest: RegularFileProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @TaskAction
    fun merge() {
        val inputManifestFile = inputManifest.asFile.get()
        val outputManifestFile = outputManifest.asFile.get()
        val publicDeeplinkManifest = createPublicDeeplinkManifest(outputManifestFile.parentFile)
        publicDeeplinkManifest.deleteOnExit()

        val mergingReport =
            ManifestMerger2.newMerger(inputManifestFile, NullLogger(), ManifestMerger2.MergeType.LIBRARY)
                .addFlavorAndBuildTypeManifests(publicDeeplinkManifest)
                .merge()

        save(
            mergingReport.getMergedDocument(MergingReport.MergedManifestKind.MERGED),
            outputManifestFile
        )
    }

    private fun createPublicDeeplinkManifest(parentDir: File): File {
        val publicDeeplinks = publicLinks.get()
        val file = File(parentDir, "AndroidManifest_public_deeplinks.xml")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.bufferedWriter().use { writer ->
            writer.write(PUBLIC_DEEPLINK_MANIFEST_HEADER.format(activityIntentFilterClass.get()))
            writer.write(formatAllDeeplinks(publicDeeplinks))
            writer.write(PUBLIC_DEEPLINK_MANIFEST_FOOTER)
        }
        return file
    }

    private fun formatAllDeeplinks(publicDeeplinks: Set<Deeplink>): String {
        return publicDeeplinks
            .groupBy { it.host }
            .map { entry ->
                formatDeeplinksForHost(entry.value)
            }
            .joinToString(separator = "\n")
    }

    /**
     * Places all deeplinks with the same host in the one <intent-filter> block.
     * Each host needs its own <intent-filter> block, either way it will be merged inconsistently.
     */
    private fun formatDeeplinksForHost(links: List<Deeplink>) =
        buildString {
            appendLine(PUBLIC_DEEPLINK_MANIFEST_INTENT_FILTER_HEADER)
            links.forEach { deeplink ->
                appendLine(formatDeeplink(deeplink))
            }
            appendLine(PUBLIC_DEEPLINK_MANIFEST_INTENT_FILTER_FOOTER)
        }

    private fun formatDeeplink(deeplink: Deeplink): String =
        """
                <data
                    android:host="${deeplink.host}"
                    android:pathPattern="${deeplink.path}"
                    android:scheme="${deeplink.scheme}"/>
        """.trimIndent()

    /**
     * Saves the [com.android.manifmerger.XmlDocument] to a file in UTF-8 encoding.
     *
     * This is a copy of a private function located in ManifestHelper.kt in Android Gradle Plugin.
     *
     * @param xmlDocument xml document to save.
     * @param out file to save to.
     */
    private fun save(xmlDocument: String?, out: File) {
        requireNotNull(xmlDocument) { "Manifest document can't be null!" }
        Files.createParentDirs(out)
        Files.asCharSink(out, Charsets.UTF_8).write(xmlDocument)
    }

    internal companion object {

        private val PUBLIC_DEEPLINK_MANIFEST_HEADER = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools">
            
                <application>
                    <activity
                        android:name="%s"
                        tools:node="merge">
        """.trimIndent()

        private const val PUBLIC_DEEPLINK_MANIFEST_FOOTER = """
                    </activity>
                </application>
            </manifest>
        """
        private val PUBLIC_DEEPLINK_MANIFEST_INTENT_FILTER_HEADER = """
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

        """.trimIndent()

        private val PUBLIC_DEEPLINK_MANIFEST_INTENT_FILTER_FOOTER = """
            </intent-filter>
        """.trimIndent()

        fun taskName(variantName: String): String = "merge${variantName.capitalize()}PublicDeeplinkManifest"
    }
}
