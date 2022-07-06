package com.avito.ci.steps

import com.avito.cd.model.BuildVariant
import groovy.lang.Closure
import org.gradle.api.Action

public open class ArtifactsConfiguration {

    internal var outputs = mutableMapOf<String, Output>()

    public var failOnSignatureError: Boolean = true

    public var publicationId: String = "local"

    public fun file(id: String, path: String) {
        registerOutput(id, Output.FileOutput(path))
    }

    @Deprecated("Use method with model.BuildVariant")
    public fun mapping(
        id: String,
        @Suppress("DEPRECATION")
        variant: com.avito.cd.BuildVariant,
        path: String
    ): Unit =
        mapping(id, variant.asNewType(), path)

    public fun mapping(
        id: String,
        variant: BuildVariant,
        path: String
    ) {
        registerOutput(id, Output.ProguardMapping(variant, path))
    }

    @Deprecated("Use method with model.BuildVariant")
    public fun apk(
        id: String,
        @Suppress("DEPRECATION")
        variant: com.avito.cd.BuildVariant,
        packageName: String,
        path: String,
        closure: Closure<Output.ApkOutput>
    ): Unit =
        apk(id, variant.asNewType(), packageName, path, closure)

    public fun apk(
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

    @Deprecated("Use method with model.BuildVariant")
    public fun apk(
        id: String,
        @Suppress("DEPRECATION")
        variant: com.avito.cd.BuildVariant,
        packageName: String,
        path: String,
        action: Action<Output.ApkOutput>
    ): Unit =
        apk(id, variant.asNewType(), packageName, path, action)

    public fun apk(
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

    @Deprecated("Use method with model.BuildVariant")
    public fun bundle(
        id: String,
        @Suppress("DEPRECATION")
        variant: com.avito.cd.BuildVariant,
        packageName: String,
        path: String,
        closure: Closure<Output.BundleOutput>
    ): Unit =
        bundle(id, variant.asNewType(), packageName, path, closure)

    public fun bundle(
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

    @Deprecated("Use method with model.BuildVariant")
    public fun bundle(
        id: String,
        @Suppress("DEPRECATION")
        variant: com.avito.cd.BuildVariant,
        packageName: String,
        path: String,
        action: Action<Output.BundleOutput>
    ): Unit =
        bundle(id, variant.asNewType(), packageName, path, action)

    public fun bundle(
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

    public fun copy(): ArtifactsConfiguration {
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

    @Suppress("DEPRECATION")
    private fun com.avito.cd.BuildVariant.asNewType() = BuildVariant(this.name.lowercase())
}
