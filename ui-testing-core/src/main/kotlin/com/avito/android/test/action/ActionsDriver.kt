package com.avito.android.test.action

import androidx.test.espresso.ViewAction

interface ActionsDriver {

    fun perform(vararg actions: ViewAction)
}
