rootProject.name = "build-logic"

pluginManagement {
    includeBuild("../build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-dependencies")
}

include("kotlin")
include("android")
include("testing")
include("checks")
include("gradle")
include("publication")
