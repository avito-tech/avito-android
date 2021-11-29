package com.avito.android.module_type

import StubModuleType
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal abstract class BaseModuleTypesTest {

    lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
    }

    fun givenProject(
        severity: Severity? = null,
        moduleType: StubModuleType,
        dependency: ModuleTypesProjectGenerator.Dependency,
        constraint: ModuleTypesProjectGenerator.Constraint,
        exclusions: Set<String> = emptySet()
    ) = ModuleTypesProjectGenerator(
        severity,
        moduleType,
        dependency,
        constraint,
        exclusions
    ).generateIn(projectDir)

    fun runCheck(
        projectDir: File,
        expectFailure: Boolean = false,
        configurationCache: Boolean = false
    ) = gradlew(
        projectDir,
        "checkModuleDependencies",
        "-Pavito.module_type.mandatoryType=true",
        expectFailure = expectFailure,
        configurationCache = configurationCache,
        useTestFixturesClasspath = true
    )
}
