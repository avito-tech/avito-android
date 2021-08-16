package com.avito.test.gradle.plugin

public interface BuildscriptClasspathPluginsSpecDsl {
    public fun applyWithBuildscript(
        buildscriptClasspath: String,
        pluginId: String
    )
}
