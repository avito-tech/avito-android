plugins {
    id("convention.detekt")
}

buildscript {

    /**
     *  workaround to load plugin classes once:
     *  ttps://youtrack.jetbrains.com/issue/KT-31643#focus=Comments-27-3510019.0-0
     */
    @Suppress("UnstableApiUsage")
    dependencies {

        /**
         * workaround till https://github.com/gradle/gradle/issues/16958 is resolved
         * most likely gradle 7.2
         */
        val libs = project.extensions.getByType<VersionCatalogsExtension>()
            .named("libs") as org.gradle.accessors.dm.LibrariesForLibs

        classpath(libs.androidGradlePlugin)
        classpath(libs.kotlinPlugin)
    }
}
