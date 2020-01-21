package com.avito.ci.steps

import com.avito.cd.BuildVariant

sealed class Output(val path: String) {

    class ProguardMapping(
        val variant: BuildVariant,
        path: String
    ) : Output(path) {
        override fun toString(): String = "proguardMappings;variant=$variant;path=$path"
    }

    class FileOutput(path: String) : Output(path) {
        override fun toString(): String = "file;path=$path"
    }

    class ApkOutput(
        val variant: BuildVariant,
        val packageName: String,
        path: String
    ) : Output(path) {
        val variantName = variant.name.toLowerCase()
        var signature: String? = null

        override fun toString(): String = "apk;variant=$variant;packageName=$packageName;path=$path"
    }

    class BundleOutput(
        val variant: BuildVariant,
        val packageName: String,
        path: String
    ) : Output(path) {
        val variantName = variant.name.toLowerCase()
        var signature: String? = null

        override fun toString(): String = "bundle;variant=$variant;packageName=$packageName;path=$path"
    }
}
