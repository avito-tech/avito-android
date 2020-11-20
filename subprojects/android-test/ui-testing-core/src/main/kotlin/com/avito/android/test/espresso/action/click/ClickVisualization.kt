package com.avito.android.test.espresso.action.click

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt

internal class ClickVisualization(
    private val x: Float,
    private val y: Float,
    private val radiusInDp: Int = 16,
    @ColorInt private val color: Int = Color.argb(0xA0, 0xFF, 0x00, 0x00)
) {

    private var attachedView: View? = null
    private var originalForeground: Drawable? = null

    fun attachTo(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            attachedView = view
            originalForeground = view.foreground
            view.foreground =
                VisualizationDrawable(originalForeground, x, y, radiusInDp.toPx(), color)
        }
    }

    fun detach() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            attachedView?.foreground = originalForeground
        }
    }

    private fun Int.toPx(): Float = this * Resources.getSystem().displayMetrics.density
}

private class VisualizationDrawable(
    val originalForeground: Drawable?,
    val x: Float,
    val y: Float,
    val radiusInPixels: Float,
    @ColorInt val color: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@VisualizationDrawable.color
    }

    override fun draw(canvas: Canvas) {
        originalForeground?.draw(canvas)
        canvas.drawCircle(x, y, radiusInPixels, paint)
    }

    override fun setAlpha(alpha: Int) {
        originalForeground?.alpha = alpha
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        originalForeground?.colorFilter = colorFilter
        paint.colorFilter = colorFilter
    }
}
