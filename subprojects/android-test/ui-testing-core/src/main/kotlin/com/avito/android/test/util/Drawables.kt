package com.avito.android.test.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

internal fun matchDrawable(
    context: Context,
    source: Drawable?,
    sourceTint: ColorStateList?,
    otherId: Int?,
    @ColorInt otherTint: Int? = null
): Boolean {
    val noOther = (otherId == null || otherId == 0)
    if (noOther && source == null) return true
    if (!noOther && source == null) return false

    return if (noOther && otherTint != null) {
        matchColors(sourceTint, otherTint)
    } else {
        matchDrawable(context, source!!, sourceTint, otherId!!, otherTint)
    }
}

private fun matchColors(left: ColorStateList?, right: Int): Boolean {
    if (left == null) return false
    if (left.isStateful) {
        throw UnsupportedOperationException("Matching for stateful ColorStateList is not supported yet")
    } else {
        return left.defaultColor == right
    }
}

private fun matchDrawable(
    context: Context,
    source: Drawable,
    sourceTint: ColorStateList?,
    @DrawableRes otherId: Int,
    @ColorInt otherTint: Int? = null
): Boolean {
    return if (otherTint != null) {
        val other: Drawable? = ContextCompat.getDrawable(context, otherId)?.wrapForTinting(otherTint)
        source.sameAs(other)
    } else {
        val other = ContextCompat.getDrawable(context, otherId) // AppCompatResources.getDrawable return with tint
        source.withoutTint(sourceTint).sameAs(other)
    }
}

private fun Drawable.withoutTint(originalTint: ColorStateList?): Drawable {
    return if (originalTint != null) {
        this.mutate().apply {
            setTintList(null)
        }
    } else {
        this
    }
}

internal fun Drawable.wrapForTinting(@ColorInt color: Int): Drawable {
    val drawable = DrawableCompat.wrap(this)
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP)
    DrawableCompat.setTint(drawable.mutate(), color)
    return drawable
}

internal fun Drawable.sameAs(other: Drawable?): Boolean {
    other ?: return false
    when {
        this is StateListDrawable && other is StateListDrawable ->
            return current.sameAs(other.current)

        this is BitmapDrawable && other is BitmapDrawable ->
            return bitmap.sameAs(other.bitmap)

        this is ColorDrawable && other is ColorDrawable ->
            return color == other.color
    }
    return this.toBitmap().sameAs(other.toBitmap())
}

internal fun Int?.getResourceName(resources: Resources): String {
    if (this == null) return ""
    if (this == 0) return "empty"
    return resources.getResourceName(this)
}

fun Drawable.toBitmap(): Bitmap {
    val drawable = this

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
