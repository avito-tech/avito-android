package com.avito.ci.steps

import com.avito.cd.BuildVariant
import groovy.lang.Closure
import org.gradle.api.Action

open class ArtifactsConfiguration {

    internal var outputs = mutableMapOf<String, Output>()

    var failOnSignatureError: Boolean = true

    var publicationId: String = "local"

    fun file(id: String, path: String) {
        registerOutput(id, Output.FileOutput(path))
    }

    fun mapping(
        id: String,
        variant: BuildVariant,
        path: String
    ) {
        registerOutput(id, Output.ProguardMapping(variant, path))
    }

    fun apk(
        id: String,
        variant: BuildVariant,
        packageName: String,
        path: String,
        closure: Closure<Output.ApkOutput>
    ) {
        check(path.endsWith(".apk")) {
            "$path is incorrect apk file. Specify full path to .apk file"
        }
        val apk = Output.ApkOutput(variant, packageName, path)
        registerOutput(id, apk)

        closure.delegate = apk
        closure.call()
    }

    fun apk(
        id: String,
        variant: BuildVariant,
        packageName: String,
        path: String,
        action: Action<Output.ApkOutput>
    ) {
        check(path.endsWith(".apk")) {
            "$path is incorrect apk file. Specify full path to .apk file"
        }
        val apk = Output.ApkOutput(variant, packageName, path)
        registerOutput(id, apk)

        action.execute(apk)
    }

    fun bundle(
        id: String,
        variant: BuildVariant,
        packageName: String,
        path: String,
        closure: Closure<Output.BundleOutput>
    ) {
        check(path.endsWith(".aab")) {
            "$path is incorrect aab file. Specify full path to .aab file"
        }
        val bundle = Output.BundleOutput(variant, packageName, path)
        registerOutput(id, bundle)

        closure.delegate = bundle
        closure.call()
    }

    fun bundle(
        id: String,
        variant: BuildVariant,
        packageName: String,
        path: String,
        action: Action<Output.BundleOutput>
    ) {
        check(path.endsWith(".aab")) {
            "$path is incorrect aab file. Specify full path to .aab file"
        }
        val bundle = Output.BundleOutput(variant, packageName, path)
        registerOutput(id, bundle)

        action.execute(bundle)
    }

    fun copy(): ArtifactsConfiguration {
        return ArtifactsConfiguration().also { copy ->
            copy.outputs = this.outputs
        }
    }

    private fun registerOutput(id: String, output: Output) {
        val oldValue = outputs.put(id, output)
        if (oldValue != null) {
            error("Artifact id=$id already registered for: $oldValue, you are trying to register $output")
        }
    }
}
