package com.avito.android.plugin

import com.avito.android.withAndroidModule
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

/**
 * To share the same room config across modules.
 */
@Suppress("unused")
public class RoomConfigPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val archPersistence = target.getMandatoryStringProperty("archPersistenceVersion")
        val kaptIncremental = target.getBooleanProperty("kapt.incremental.apt", default = false)

        target.withAndroidModule {
            target.dependencies.add("implementation", "androidx.room:room-runtime:$archPersistence")
            target.dependencies.add("implementation", "androidx.room:room-rxjava2:$archPersistence")

            target.dependencies.add("testImplementation", "androidx.room:room-testing:$archPersistence")
        }

        target.plugins.withId("kotlin-kapt") {

            target.dependencies.add("kapt", "androidx.room:room-compiler:$archPersistence")

            target.extensions.getByType<KaptExtension>().run {
                arguments {
                    arg("room.incremental", kaptIncremental)
                    // room.schemaLocation is configured by https://github.com/gradle/android-cache-fix-gradle-plugin#roomschemalocationworkaround
                }
            }
        }
    }
}
