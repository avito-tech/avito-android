plugins {
    `kotlin-dsl`
    id("com.avito.android.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation(libs.androidGradlePlugin)
}

repositories {
    jcenter()
    google()
}
