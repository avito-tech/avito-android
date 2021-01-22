plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("robolectric-prefetch") {
            id = "com.avito.android.robolectric-prefetch"
            implementationClass = "com.avito.android.RobolectricPrefetchPlugin"
            displayName = "Robolectric prefetch plugin"
        }
    }
}
