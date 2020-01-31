package com.avito.android.test.matcher

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.test.espresso.matcher.BoundedMatcher
import com.avito.android.test.util.getResourceName
import com.avito.android.test.util.matchDrawable
import org.hamcrest.Description

class CompoundDrawableMatcher(
    @DrawableRes private val left: Int? = null,
    @DrawableRes private val top: Int? = null,
    @DrawableRes private val right: Int? = null,
    @DrawableRes private val bottom: Int? = null,
    @ColorInt private val tint: Int? = null
) : BoundedMatcher<View, TextView>(TextView::class.java) {

    private var description: String? = null

    override fun describeTo(description: Description) {
        description.appendText("has compound drawables: ${this.description}")
    }

    override fun matchesSafely(item: TextView): Boolean {
        val context = item.context
        val compoundDrawables = item.compoundDrawables
        this.description = getDescription(context.resources)

        return left.matchDrawable(context, compoundDrawables[LEFT], tint) &&
                right.matchDrawable(context, compoundDrawables[RIGHT], tint) &&
                top.matchDrawable(context, compoundDrawables[TOP], tint) &&
                bottom.matchDrawable(context, compoundDrawables[BOTTOM], tint)
    }

    private fun getDescription(resources: Resources): String {
        return "left=${left.getResourceName(resources)}, " +
                "top=${top.getResourceName(resources)}, " +
                "right=${right.getResourceName(resources)}, " +
                "bottom=${bottom.getResourceName(resources)}," +
                "with tint=${tint?.toString(16) ?: "not applied"}"
    }
}

private const val LEFT = 0
private const val TOP = 1
private const val RIGHT = 2
private const val BOTTOM = 3
