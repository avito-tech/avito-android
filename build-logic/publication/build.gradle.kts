plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.androidGradle)
    implementation(libs.okhttp)
    implementation(libs.kotson)
}

gradlePlugin {
    plugins {
        create("publish-gradle-plugin") {
            id = "convention.publish-gradle-plugin"
            implementationClass = "com.avito.PublishGradlePlugin"
        }
        create("publish-kotlin-library") {
            id = "convention.publish-kotlin-library"
            implementationClass = "com.avito.PublishKotlinLibraryPlugin"
        }
        create("publish-android-library") {
            id = "convention.publish-android-library"
            implementationClass = "com.avito.PublishAndroidLibraryPlugin"
        }
    }
}
