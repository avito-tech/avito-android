plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(libs.versionsGradle)
    implementation(libs.detektGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // This works
    //implementation("com.avito.android:build-trace:local")
}
