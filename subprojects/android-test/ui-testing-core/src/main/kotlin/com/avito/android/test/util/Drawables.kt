package com.avito.android.test.util

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

internal fun Drawable.isSame(
    context: Context,
    @DrawableRes resId: Int,
    @ColorInt tint: Int? = null
): Boolean {
    val fromContext = ContextCompat.getDrawable(context, resId)
    return if (tint != null) {
        isSame(fromContext?.wrapForTinting(tint))
    } else {
        val tintedFromContext = AppCompatResources.getDrawable(context, resId)
        this.isSame(fromContext)
            || this.isSame(tintedFromContext)
    }
}

internal fun Drawable.wrapForTinting(@ColorInt color: Int): Drawable {
    val drawable = DrawableCompat.wrap(this)
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP)
    DrawableCompat.setTint(drawable.mutate(), color)
    return drawable
}

internal fun Drawable.isSame(other: Drawable?): Boolean {
    other ?: return false
    when {
        this is StateListDrawable && other is StateListDrawable -> {
            return current.isSame(other.current)
        }
        this is BitmapDrawable && other is BitmapDrawable -> {
            return bitmap.sameAs(other.bitmap)
        }
        this is ColorDrawable && other is ColorDrawable -> {
            return color == other.color
        }
    }
    return this.toBitmap().sameAs(other.toBitmap())
}

internal fun Int?.getResourceName(resources: Resources): String {
    if (this == null) return ""
    if (this == 0) return "empty"
    return resources.getResourceName(this)
}

// fixme false negative?
@SuppressLint("ResourceType")
internal fun Int?.matchDrawable(
    context: Context,
    drawable: Drawable?,
    @ColorInt tint: Int? = null
): Boolean {
    if (this == null) return true
    if (this == 0 && drawable == null) return true
    return drawable?.isSame(context, this, tint) ?: false
}

internal fun Drawable.toBitmap(): Bitmap {
    val drawable = this

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
