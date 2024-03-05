package com.avito.android.test.action

import androidx.test.espresso.ViewAction

public interface ActionsDriver {

    public fun perform(vararg actions: ViewAction)
}
