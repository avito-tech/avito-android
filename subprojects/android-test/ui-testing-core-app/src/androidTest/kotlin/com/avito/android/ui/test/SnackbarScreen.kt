package com.avito.android.ui.test

import com.avito.android.test.page_object.PageObject
import org.hamcrest.Matchers

class SnackbarScreen : PageObject() {

    val snackbar =
        @Suppress("DEPRECATION")
        com.avito.android.test.page_object.SnackbarElement()

    fun snackbar(text: String) =
        @Suppress("DEPRECATION")
        com.avito.android.test.page_object.SnackbarElement(Matchers.equalTo(text))
}
