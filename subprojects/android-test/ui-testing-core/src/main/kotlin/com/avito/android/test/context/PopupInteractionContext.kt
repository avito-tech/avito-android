package com.avito.android.test.context

import android.view.View
import androidx.test.espresso.matcher.RootMatchers
import com.avito.android.test.SimpleInteractionContext
import org.hamcrest.Matcher

class PopupInteractionContext(
    matcher: Matcher<View>,
) : SimpleInteractionContext(
    matcher = matcher,
    rootMatcher = RootMatchers.isPlatformPopup()
)
