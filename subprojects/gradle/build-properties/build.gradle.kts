plugins {
    id("kotlin")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    `maven-publish`
    id("com.jfrog.bintray")
}

val androidGradlePluginVersion: String by project

dependencies {
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")

    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildProperties") {
            id = "com.avito.android.build-properties"
            implementationClass = "com.avito.android.info.BuildPropertiesPlugin"
            displayName = "Build properties"
        }
    }
}
