package com.avito.android.impact

import android.content.res.AssetManager

class ImpactAnalysisAssets(private val assetManager: AssetManager) {

    private val impactAnalysisFolder = "impactAnalysisMeta"

    val isImpactMetadataAvailable: Boolean
        get() = !assetManager.list(impactAnalysisFolder).isNullOrEmpty()

    fun readSymbolList(): Map<String, Int> {
        return if (!isImpactMetadataAvailable) {
            emptyMap()
        } else {
            assetManager.open("$impactAnalysisFolder/R.txt")
                .bufferedReader()
                .useLines { readSymbolList(it) }
        }
    }

    fun readModuleIds(): Map<String, List<String>> {
        return if (!isImpactMetadataAvailable) {
            emptyMap()
        } else {
            val files = assetManager.list(impactAnalysisFolder)
            requireNotNull(files) { "$impactAnalysisFolder should not be empty!" }

            files
                .filter { it != "R.txt" }
                .map { fileName ->
                    modulePathFromFileName(fileName) to assetManager.open("${impactAnalysisFolder}/$fileName")
                        .bufferedReader()
                        .useLines {
                            readModuleIds(it)
                        }
                }
                .toMap()
        }
    }

    private fun modulePathFromFileName(fileName: String): String {
        return ":" + fileName.substringBefore('_').replace('-', ':')
    }
}
