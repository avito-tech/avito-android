package com.avito.android.test.matcher

import android.annotation.SuppressLint
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.view.menu.ActionMenuItemView

@SuppressLint("RestrictedApi")
class DrawableMatcherActionIcon(
    @DrawableRes private val src: Int? = null,
    @ColorRes private val tint: Int? = null
) : DrawableMatcher<ActionMenuItemView>(
    { it.itemData.icon },
    { it.itemData.iconTintList },
    src,
    tint,
    ActionMenuItemView::class.java
)
