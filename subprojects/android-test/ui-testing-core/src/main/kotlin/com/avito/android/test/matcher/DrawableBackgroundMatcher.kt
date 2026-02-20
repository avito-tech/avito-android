package com.avito.android.test.matcher

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

public class DrawableBackgroundMatcher(
    @param:DrawableRes private val src: Int? = null,
    @param:ColorRes private val tint: Int? = null
) :
    DrawableMatcher<View>(
        { it.background },
        { it.backgroundTintList },
        src,
        tint,
        View::class.java
    )
