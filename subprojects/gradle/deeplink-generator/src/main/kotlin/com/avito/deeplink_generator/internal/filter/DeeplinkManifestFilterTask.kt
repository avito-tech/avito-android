package com.avito.deeplink_generator.internal.filter

import com.avito.capitalize
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.Namespace
import groovy.xml.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
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
 * Task for filtering out deeplink schemes from application's AndroidManifest.
 */
@CacheableTask
internal abstract class DeeplinkManifestFilterTask : DefaultTask() {

    @get:Input
    abstract val forbiddenSchemes: SetProperty<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputManifest: RegularFileProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @TaskAction
    fun merge() {
        val inputManifestFile = inputManifest.asFile.get()
        val outputManifestFile = outputManifest.asFile.get()

        val publicDeeplinkManifest = createPublicDeeplinkManifest(inputManifestFile, outputManifestFile.parentFile)
        publicDeeplinkManifest.deleteOnExit()

        outputManifestFile.bufferedWriter().use { writer ->
            writer.write(publicDeeplinkManifest.readText())
        }
    }

    private fun createPublicDeeplinkManifest(inputManifest: File, parentDir: File?): File {
        val file = File(parentDir, "AndroidManifest_public_composed_deeplinks.xml")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        val xml = XmlParser().parse(inputManifest)
        val androidNS = Namespace("http://schemas.android.com/apk/res/android", "android")
        val forbiddenSchemes = forbiddenSchemes.get()

        (xml["application"] as NodeList).asNodes()
            .flatMap { it.children("activity") }
            .flatMap { it.children("intent-filter") }
            .flatMap { it.children("data") }
            .filter { data ->
                forbiddenSchemes.any { scheme ->
                    data.attr(androidNS, "scheme")?.contains(scheme) == true
                }
            }
            .forEach { data -> data.parent().remove(data) }

        file.bufferedWriter().use { writer ->
            writer.write(XmlUtil.serialize(xml))
        }

        return file
    }

    private fun NodeList.asNodes(): Sequence<Node> =
        iterator().asSequence().filterIsInstance<Node>()

    private fun Node.children(name: String): Sequence<Node> =
        (this[name] as NodeList).asNodes()

    private fun Node.attr(ns: Namespace, name: String): String? =
        attribute(ns[name])?.toString()

    internal companion object {
        fun taskName(variantName: String): String = "filter${variantName.capitalize()}PublicDeeplinkManifest"
    }
}
