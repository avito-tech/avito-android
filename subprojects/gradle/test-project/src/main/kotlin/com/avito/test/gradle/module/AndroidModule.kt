package com.avito.test.gradle.module

public interface AndroidModule : Module {
    public override val buildFileImports: List<String>
    public val packageName: String
    public val enableKotlinAndroidPlugin: Boolean
}
