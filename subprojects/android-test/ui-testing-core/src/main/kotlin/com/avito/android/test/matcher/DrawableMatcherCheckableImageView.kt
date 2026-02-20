package com.avito.android.test.matcher

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.android.material.internal.CheckableImageButton

public class DrawableMatcherCheckableImageView(
    @param:DrawableRes private val src: Int? = null,
    @param:ColorRes private val tint: Int? = null
) :
    DrawableMatcher<CheckableImageButton>(
        { it.drawable },
        { it.imageTintList },
        src,
        tint,
        CheckableImageButton::class.java
    )
