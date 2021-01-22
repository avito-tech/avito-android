plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:robolectric-prefetch"))

    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("robolectric-convention") {
            id = "com.avito.android.robolectric-convention"
            implementationClass = "com.avito.android.plugin.RobolectricConventionPlugin"
            displayName = "Robolectric convention for Avito"
        }
    }
}
