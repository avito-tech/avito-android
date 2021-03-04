package com.avito.module.dependencies

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseFindAndroidAppTaskTest {
    lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
        DependenciesGraphTestProject.generate(projectDir)
    }

    abstract fun `find one suitable app - advice this app`()

    abstract fun `find multiple same suitable apps - advice that you could choose both`()

    abstract fun `find multiple suitable apps - advice with that has least dependencies`()

    abstract fun `don't find any suitable app - advice that there are no apps`()
}
