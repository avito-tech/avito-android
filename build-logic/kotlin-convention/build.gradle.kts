plugins {
    `kotlin-dsl`
    id("com.avito.android.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:libraries")
    implementation(platform("com.avito.android.infra:platforms"))
    implementation(libs.kotlinPlugin)
}

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
