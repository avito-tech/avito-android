plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
