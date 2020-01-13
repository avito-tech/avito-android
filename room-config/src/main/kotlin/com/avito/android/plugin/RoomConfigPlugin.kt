package com.avito.android.plugin

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import java.io.File

/**
 * To share the same room config across modules.
 *
 * Plugin adds necessary dependencies and sets room.schemaLocation to point to <module>/room-schemas.
 */
@Suppress("unused")
class RoomConfigPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val archPersistence = target.getMandatoryStringProperty("archPersistence")

        target.dependencies.add("implementation", "androidx.room:room-runtime:$archPersistence")
        target.dependencies.add("implementation", "androidx.room:room-rxjava2:$archPersistence")

        target.dependencies.add("kapt", "androidx.room:room-compiler:$archPersistence")

        target.dependencies.add("testImplementation", "androidx.room:room-testing:$archPersistence")

        target.extensions.getByType<KaptExtension>().run {
            arguments {
                arg("room.schemaLocation", File(target.projectDir, "room-schemas").absolutePath)
            }
        }
    }

}
