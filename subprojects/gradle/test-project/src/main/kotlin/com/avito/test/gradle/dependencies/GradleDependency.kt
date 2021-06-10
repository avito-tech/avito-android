package com.avito.test.gradle.dependencies

import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.External
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Platform
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Project
import org.gradle.util.Path

sealed class GradleDependency : GradleScriptCompatible {

    data class Raw(val rawDependency: String) : GradleDependency() {
        override fun getScriptRepresentation() = rawDependency
    }

    data class Safe(
        private val configuration: CONFIGURATION,
        private val dep: Coordinate
    ) : GradleDependency() {

        enum class CONFIGURATION(private val representation: String) : GradleScriptCompatible {
            COMPILE("compile"),
            IMPLEMENTATION("implementation"),
            TEST_IMPLEMENTATION("testImplementation"),
            ANDROID_TEST_IMPLEMENTATION("androidTestImplementation"),
            API("api");

            override fun getScriptRepresentation() = representation
        }

        sealed class Coordinate : GradleScriptCompatible {

            data class External(val coordinates: String) : Coordinate() {
                override fun getScriptRepresentation() = "\"$coordinates\""
            }

            data class Project(val path: Path) : Coordinate() {
                override fun getScriptRepresentation() = "project(\"${path.path}\")"
            }

            data class Platform(val coordinate: Coordinate) : Coordinate() {
                override fun getScriptRepresentation(): String {
                    return "platform(${
                        when (coordinate) {
                            is External -> coordinate.getScriptRepresentation()
                            is Project -> coordinate.getScriptRepresentation()
                            else -> throw IllegalArgumentException(
                                "Platform coordinate could be either External or Project"
                            )
                        }
                    })"
                }
            }
        }

        override fun getScriptRepresentation(): String {
            return "${configuration.getScriptRepresentation()}(${dep.getScriptRepresentation()})"
        }

        companion object {

            fun external(
                coordinate: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, External(coordinate))

            fun project(
                path: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, Project(Path.path(path)))

            fun platformProject(
                path: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(
                configuration,
                Platform(Project(Path.path(path)))
            )

            fun platformExternal(
                coordinate: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, External(coordinate))
        }
    }
}
