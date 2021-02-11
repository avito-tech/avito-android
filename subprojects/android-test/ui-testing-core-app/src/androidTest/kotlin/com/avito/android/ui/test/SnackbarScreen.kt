@file:Suppress("DEPRECATION")

package com.avito.android.ui.test

import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.SnackbarElement
import org.hamcrest.Matchers

class SnackbarScreen : PageObject() {

    val snackbar = SnackbarElement()

    fun snackbar(text: String) = SnackbarElement(Matchers.equalTo(text))
}
