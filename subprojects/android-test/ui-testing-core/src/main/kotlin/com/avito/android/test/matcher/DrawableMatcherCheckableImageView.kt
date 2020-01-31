package com.avito.android.test.matcher

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.android.material.internal.CheckableImageButton

class DrawableMatcherCheckableImageView(
    @DrawableRes private val src: Int? = null,
    @ColorRes private val tint: Int? = null
) :
    DrawableMatcher<CheckableImageButton>(
        { it.drawable },
        src,
        tint,
        CheckableImageButton::class.java
    )
