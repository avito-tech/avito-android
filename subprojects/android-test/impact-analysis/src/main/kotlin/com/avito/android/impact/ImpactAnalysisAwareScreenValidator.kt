package com.avito.android.impact

/**
 * Single instance for the whole test suite recommended
 */
class ImpactAnalysisAwareScreenValidator(
    private val moduleToIds: Map<String, List<String>>,
    private val idsSymbolList: Map<String, Int>
) {

    sealed class Result {

        object OK : Result()

        /**
         * Identifies that problem in the impact analysis mechanism itself,
         * and you cannot fix it via configuration of a project (e.g. changing rootId/modulePath parameters)
         */
        data class ImpactAnalysisError(val reason: String) : Result()

        /**
         * Issue in screen configuration, most likely will be fixed with corrected modulePath or rootId
         */
        data class ConfigurationError(val reason: String) : Result()
    }

    /**
     * checks impact analysis aware screen for (module <-> rootId) consistency
     * more info: https://github.com/avito-tech/avito-android/issues/292
     *
     * should be called as early as possible on first screen access in tests
     */
    fun validateScreen(screenClassName: String, rootId: Int, module: String): Result {

        val availableModules = moduleToIds.keys

        if (!availableModules.contains(module)) {
            return Result.ConfigurationError("$screenClassName.modulePath=$module no such module available in project; available modules are: $availableModules")
        }

        val moduleIdsNames = moduleToIds[module] ?: emptyList()

        val moduleIds = moduleIdsNames.map {
            val id = idsSymbolList[it]

            @Suppress("IfThenToElvis")
            if (id == null) {
                return Result.ImpactAnalysisError("Can't find id $it in app's symbol list")
            } else {
                id
            }
        }

        if (!moduleIds.contains(rootId)) {
            return Result.ConfigurationError("$screenClassName.rootId=$rootId not found in module $module")
        }

        return Result.OK
    }
}
