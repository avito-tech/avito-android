rootProject.name = "build-logic"

pluginManagement {
    includeBuild("../build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-dependencies")
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
