package com.avito.test.gradle.dependencies

import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.External
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Platform
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Project
import org.funktionale.either.Either
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
                override fun getScriptRepresentation() = "'$coordinates'"
            }

            data class Project(val path: Path) : Coordinate() {
                override fun getScriptRepresentation() = "project('${path.path}')"
            }

            data class Platform(val coordinate: Either<External, Project>) : Coordinate() {
                override fun getScriptRepresentation(): String {
                    return "platform(${
                        coordinate.fold(
                            { external -> external.getScriptRepresentation() },
                            { project -> project.getScriptRepresentation() }
                        )
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
                Platform(Either.right(Project(Path.path(path))))
            )

            fun platformExternal(
                coordinate: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, Platform(Either.left(External(coordinate))))
        }
    }
}
