plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":android"))
    implementation(project(":kotlin-dsl-support"))
}

gradlePlugin {
    plugins {
        create("robolectricConfig") {
            id = "com.avito.android.robolectric"
            implementationClass = "com.avito.android.plugin.RobolectricPlugin"
        }
    }
}
