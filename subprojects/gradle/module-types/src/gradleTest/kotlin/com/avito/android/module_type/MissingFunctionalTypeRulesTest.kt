package com.avito.android.module_type

import org.junit.jupiter.api.Test

internal class MissingFunctionalTypeRulesTest : BaseModuleTypesTest() {

    @Test
    fun `missing rule fails even without modules of that type`() {
        givenProject(
            config = ModuleTypesProjectGenerator.ModuleTypesProjectConfig(
                missingFunctionalTypeRule = FunctionalType.TestDebug,
            )
        )

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Missing betweenFunctionalTypes rules")
            .outputContains("Missing rules for: TestDebug")
    }
}
