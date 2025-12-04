package com.avito.test.gradle.plugin

public interface PluginsSpecDsl {
    public fun id(id: String): PluginSpec
    public fun alias(alias: String): PluginSpec
}
