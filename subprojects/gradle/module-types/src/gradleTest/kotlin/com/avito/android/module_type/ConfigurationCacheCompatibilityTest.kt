package com.avito.android.module_type

import FeatureModule
import LibraryModule
import com.avito.android.module_type.ModuleTypesProjectGenerator.Constraint
import com.avito.android.module_type.ModuleTypesProjectGenerator.Dependency
import org.junit.jupiter.api.Test

internal class ConfigurationCacheCompatibilityTest : BaseModuleTypesTest() {

    @Test
    fun `cache reused - configuration with applied plugin`() {
        givenProject(
            moduleType = LibraryModule,
            dependency = Dependency(LibraryModule),
            constraint = Constraint(module = LibraryModule, dependency = FeatureModule)
        )

        runCheck(projectDir, configurationCache = true).assertThat().buildSuccessful()

        runCheck(projectDir, configurationCache = true).assertThat().buildSuccessful().configurationCachedReused()
    }
}
