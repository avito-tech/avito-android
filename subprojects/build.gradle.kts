plugins {
    id("convention.lifecycle")
    // accessing version catalog here is blocked by IDE false-postive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    id("com.autonomousapps.dependency-analysis") version "0.74.0"
    id("convention.dependency-updates")
    id("convention.detekt")
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     */
    dependencies {
        classpath(libs.androidGradle)
        classpath(libs.kotlinGradle)

        /**
         * com.autonomousapps.dependency-analysis depends on older version of okio, and it's resolved for
         * our instrumentation-tests plugin in subprojects in runtime
         */
        classpath(libs.okio)
    }
}
