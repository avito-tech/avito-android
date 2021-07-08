includeBuild("../build-logic")
includeBuild("../subprojects")

include(":test-runner")

pluginManagement {
    includeBuild("../build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-cache")
    id("convention-dependencies")
    id("convention-scan")
}
