plugins {
    // accessing version catalog here is blocked by IDE false-positive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    base
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  https://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     *
     *  Causes this instead:
     *  An exception occurred applying plugin request [id: 'org.gradle.kotlin.kotlin-dsl', version: '2.1.7']
     * > Failed to apply plugin class 'org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper'.
     * > Could not create an instance of type org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension.
     * > Companion
     */
    dependencies {
        classpath(libs.kotlinGradle)
    }
}
