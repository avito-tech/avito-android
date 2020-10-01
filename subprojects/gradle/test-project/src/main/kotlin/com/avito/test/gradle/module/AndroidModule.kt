package com.avito.test.gradle.module

interface AndroidModule : Module {
    val packageName: String
    val enableKotlinAndroidPlugin: Boolean
}
