package com.avito.test.gradle.module

public interface AndroidModule : Module {
    public val packageName: String
    public val enableKotlinAndroidPlugin: Boolean
}
