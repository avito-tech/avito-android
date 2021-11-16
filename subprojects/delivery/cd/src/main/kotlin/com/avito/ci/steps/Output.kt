package com.avito.ci.steps

import com.avito.cd.BuildVariant

public sealed class Output(public val path: String) {

    public class ProguardMapping(
        public val variant: BuildVariant,
        path: String
    ) : Output(path) {
        override fun toString(): String = "proguardMappings;variant=$variant;path=$path"
    }

    public class FileOutput(path: String) : Output(path) {
        override fun toString(): String = "file;path=$path"
    }

    public class ApkOutput(
        public val variant: BuildVariant,
        public val packageName: String,
        path: String
    ) : Output(path) {
        public val variantName: String = variant.name.lowercase()
        public var signature: String? = null

        override fun toString(): String = "apk;variant=$variant;packageName=$packageName;path=$path"
    }

    public class BundleOutput(
        public val variant: BuildVariant,
        public val packageName: String,
        path: String
    ) : Output(path) {
        public val variantName: String = variant.name.lowercase()
        public var signature: String? = null

        override fun toString(): String = "bundle;variant=$variant;packageName=$packageName;path=$path"
    }
}
