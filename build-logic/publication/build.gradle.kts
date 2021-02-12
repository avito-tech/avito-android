plugins {
    `kotlin-dsl`
    id("com.avito.android.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.bintrayPlugin)
    implementation(libs.androidGradlePlugin)
}

repositories {
    jcenter()
    google()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
