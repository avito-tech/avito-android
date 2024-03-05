package com.avito.android.test

import android.view.View
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.checks.ChecksDriver
import org.hamcrest.Matcher

public interface InteractionContext : ActionsDriver, ChecksDriver {

    public fun provideChildContext(matcher: Matcher<View>): InteractionContext
}
