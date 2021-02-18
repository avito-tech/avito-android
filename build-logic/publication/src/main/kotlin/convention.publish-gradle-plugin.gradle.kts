plugins {
    id("convention.publish-kotlin-base")
    id("convention.publish-release")
    id("convention.publish-artifactory")
    id("java-gradle-plugin")
}

gradlePlugin {
    isAutomatedPublishing = true
}
