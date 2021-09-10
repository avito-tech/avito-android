plugins {
    id("convention.lifecycle")
    id("convention.detekt")
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     */
    dependencies {
        classpath(libs.kotlinGradle)
        classpath(libs.androidGradle)
    }
}
