package com.avito.android.impact

/**
 * Single instance for whole test suite recommended
 */
class ImpactAnalysisAwareScreenValidator(
    private val moduleToIds: Map<String, List<String>>,
    private val idsSymbolList: Map<String, Int>
) {

    /**
     * checks impact analysis aware screen for module<->rootId consistency
     * more info: https://github.com/avito-tech/avito-android/issues/292
     */
    fun validateScreen(screenClassName: String, rootId: Int, module: String) {

        val availableModules = moduleToIds.keys
        require(availableModules.contains(module)) { "$screenClassName.modulePath=$module no such module available in project; available modules are: $availableModules" }

        val moduleIdsNames = moduleToIds[module] ?: emptyList()

        val moduleIds = moduleIdsNames.map {
            val id = idsSymbolList[it]
            requireNotNull(id) { "Can't find id $it in app's symbol list" }
        }

        require(moduleIds.contains(rootId)) { "$screenClassName.rootId=$rootId not found in module $module" }
    }
}
