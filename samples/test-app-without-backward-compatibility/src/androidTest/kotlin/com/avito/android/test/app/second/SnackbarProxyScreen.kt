package com.avito.android.test.app.second

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement

class SnackbarProxyScreen : PageObject() {
    private val showToastButton = element<ViewElement>(ViewMatchers.withId(R.id.show_snackbar_short))
    private val showToastDelayedButton = element<ViewElement>(ViewMatchers.withId(R.id.show_snackbar_delayed))

    fun clickShowSnackbar() {
        showToastButton.click()
    }

    fun clickShowSnackbarDelayed() {
        showToastDelayedButton.click()
    }

}
