package com.avito.android.impact

import android.content.res.AssetManager
import android.util.Log

/**
 * 1. Create validator with this function and place instance somewhere in static (because InstrumentationRegistry already a static instance)
 *    assetManager available via InstrumentationRegistry.context.assetManager
 * 2. Create base class for all Screens in project that implements Screen and ImpactAnalysisAware
 * 3. Access created instance from init method of base screen and
 *    call ImpactAnalysisAwareScreenValidator.validateScreen(screenClassName: String, rootId: Int, module: String)
 */
fun createImpactAnalysisScreenValidator(assetManager: AssetManager): ImpactAnalysisAwareScreenValidator? {

    val assets = ImpactAnalysisAssets(assetManager)

    return if (assets.isImpactMetadataAvailable) {
        ImpactAnalysisAwareScreenValidator(
            moduleToIds = assets.readModuleIds(),
            idsSymbolList = assets.readSymbolList()
        )
    } else {
        Log.w("ImpactAnalysis", "Impact analysis metadata unavailable")
        null
    }
}
