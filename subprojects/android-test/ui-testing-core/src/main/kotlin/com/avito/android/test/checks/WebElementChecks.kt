package com.avito.android.test.checks

import androidx.test.espresso.web.sugar.Web

public class WebElementChecks(private val interaction: Web.WebInteraction<Void>) {

    public fun isDisplayed() {
        interaction
    }
}
