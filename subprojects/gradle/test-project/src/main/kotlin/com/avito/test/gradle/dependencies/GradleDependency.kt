package com.avito.test.gradle.dependencies

import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.External
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Platform
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Coordinate.Project
import org.gradle.util.Path

public sealed class GradleDependency : GradleScriptCompatible {

    public data class Raw(val rawDependency: String) : GradleDependency() {
        override fun getScriptRepresentation(): String = rawDependency
    }

    public data class Safe(
        private val configuration: CONFIGURATION,
        private val dep: Coordinate
    ) : GradleDependency() {

        public enum class CONFIGURATION(private val representation: String) : GradleScriptCompatible {
            IMPLEMENTATION("implementation"),
            TEST_IMPLEMENTATION("testImplementation"),
            ANDROID_TEST_IMPLEMENTATION("androidTestImplementation"),
            API("api");

            override fun getScriptRepresentation(): String = representation
        }

        public sealed class Coordinate : GradleScriptCompatible {

            public data class External(val coordinates: String) : Coordinate() {
                override fun getScriptRepresentation(): String = "\"$coordinates\""
            }

            public data class Project(val path: Path) : Coordinate() {
                override fun getScriptRepresentation(): String = "project(\"${path.path}\")"
            }

            public data class TypesafeProjectAccessor(val accessor: String) : Coordinate() {
                override fun getScriptRepresentation(): String = "projects.$accessor"
            }

            public data class Platform(val coordinate: Coordinate) : Coordinate() {
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

        public companion object {

            public fun external(
                coordinate: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, External(coordinate))

            public fun project(
                path: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, Project(Path.path(path)))

            public fun typesafeProjectAccessor(
                path: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, Coordinate.TypesafeProjectAccessor(path))

            public fun platformProject(
                path: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(
                configuration,
                Platform(Project(Path.path(path)))
            )

            public fun platformExternal(
                coordinate: String,
                configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION
            ): Safe = Safe(configuration, External(coordinate))
        }
    }
}
