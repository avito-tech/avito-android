package com.avito.test.gradle.module

public interface AndroidModule : Module {
    public override val imports: List<String>
    public val packageName: String
    public val enableKotlinAndroidPlugin: Boolean
}
