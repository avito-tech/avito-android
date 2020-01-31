package com.avito.plugin

import com.android.builder.model.BuildType

open class SignExtension {

    internal val apkSignTokens = mutableMapOf<String, String?>()

    internal val bundleSignTokens = mutableMapOf<String, String?>()

    //todo rename to url
    var host: String? = null

    fun apk(variant: BuildType, token: String?) {
        apkSignTokens[variant.name] = token
    }

    fun bundle(variant: BuildType, token: String?) {
        bundleSignTokens[variant.name] = token
    }
}
