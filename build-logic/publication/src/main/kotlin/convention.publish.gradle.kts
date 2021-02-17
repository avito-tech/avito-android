plugins {
    `maven-publish`
}

group = "com.avito.android"

// avito.project.version override from CI property, looks like it could be simplified to single one
@Suppress("UnstableApiUsage")
version = providers.systemProperty("avito.project.version").forUseAtConfigurationTime()
    .orElse(providers.gradleProperty("projectVersion").forUseAtConfigurationTime())
    .get()
