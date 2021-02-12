plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
