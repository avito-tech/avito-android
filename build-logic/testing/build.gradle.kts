plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
    implementation(libs.gradleTestRetryPlugin)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
