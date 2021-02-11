plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("robolectricConfig") {
            id = "com.avito.android.robolectric"
            implementationClass = "com.avito.android.plugin.RobolectricPlugin"
            displayName = "Robolectric config"
        }
    }
}
