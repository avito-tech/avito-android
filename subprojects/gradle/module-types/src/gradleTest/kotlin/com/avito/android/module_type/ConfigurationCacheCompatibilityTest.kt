package com.avito.android.module_type

import org.junit.jupiter.api.Test

internal class ConfigurationCacheCompatibilityTest : BaseModuleTypesTest() {

    @Test
    fun `cache reused - configuration with applied plugin`() {
        givenProject(
            config = ModuleTypesProjectGenerator.ModuleTypesProjectConfig()
        )

        runCheck(projectDir, configurationCache = true).assertThat().buildSuccessful()

        runCheck(projectDir, configurationCache = true).assertThat().buildSuccessful().configurationCachedReused()
    }
}
