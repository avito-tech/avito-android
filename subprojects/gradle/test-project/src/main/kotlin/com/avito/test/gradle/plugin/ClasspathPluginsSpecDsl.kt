package com.avito.test.gradle.plugin

public interface ClasspathPluginsSpecDsl {
    public fun classpathPlugin(
        pluginClasspath: String,
        pluginId: String
    )
}
