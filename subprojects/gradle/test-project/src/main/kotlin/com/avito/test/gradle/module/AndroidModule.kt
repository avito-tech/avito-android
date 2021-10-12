package com.avito.test.gradle.module

public interface AndroidModule : Module {
    public val imports: List<String>
    public val packageName: String
    public val enableKotlinAndroidPlugin: Boolean

    public fun imports(): String = imports.joinToString(separator = "\n")
}
