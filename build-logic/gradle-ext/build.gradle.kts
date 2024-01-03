plugins {
    kotlin("jvm")
}

group = "com.avito.android.buildlogic"

dependencies {
    compileOnly(gradleApi())
    // to access LibrariesForLibs
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
