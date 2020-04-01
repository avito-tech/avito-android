package com.avito.android.impact

interface ImpactAnalysisAware {

    /**
     * Should be a full path to a module containing it's rootId
     *
     * Links "Screen" PageObject and "Screen" (android.view.View) in app code
     * for impact analysis in ui tests
     */
    val modulePath: String
}
