plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(libs.kotlinGradle)
    implementation(libs.androidGradle)
    implementation(libs.nebulaIntegTest)
    implementation(libs.testRetryGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
