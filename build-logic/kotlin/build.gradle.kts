plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(projects.testing)
    implementation(libs.kotlinGradle)
    implementation(libs.kotlinx.serialization.gradle)
    implementation(libs.kspGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
