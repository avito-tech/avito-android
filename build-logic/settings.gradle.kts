rootProject.name = "build-logic"

pluginManagement {
    // See rationale inside this script
    apply(from = "../build-logic-settings/dependency-plugin/pluginManagement-shared.settings.gradle.kts")

    includeBuild("../build-logic-settings")
}

plugins {
    id("convention-dependencies")
}

val parentBuild = gradle.parent

/**
 * --dry-run on root build executes tasks in a composite build
 * Workaround to https://github.com/gradle/gradle/issues/2517
 */
if (parentBuild != null && parentBuild.startParameter.isDryRun) {
    gradle.startParameter.isDryRun = true
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include("kotlin")
include("android")
include("testing")
include("checks")

/**
 * renamed from 'gradle' to prevent IDE resolution conflict:
 * usages of "typesafe project accessors", e.g. `projects.gradle.someProject` was red in IDE
 * build was fine however
 */
include("gradle-ext")
include("publication")
