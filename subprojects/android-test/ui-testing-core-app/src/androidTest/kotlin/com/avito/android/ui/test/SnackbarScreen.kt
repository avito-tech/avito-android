package com.avito.android.ui.test

import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.ui.R
import org.hamcrest.Matchers

class SnackbarScreen : SimpleScreen() {

    override val rootId: Int = R.id.root

    val snackbar =
        @Suppress("DEPRECATION")
        com.avito.android.test.page_object.SnackbarElement()

    fun snackbar(text: String) =
        @Suppress("DEPRECATION")
        com.avito.android.test.page_object.SnackbarElement(Matchers.equalTo(text))
}
