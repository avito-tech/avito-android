rootProject.name = "build-logic"

include("kotlin")
include("android")
include("testing")
include("checks")
include("gradle")
include("publication")

pluginManagement {
    includeBuild("../build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-dependencies")
}
