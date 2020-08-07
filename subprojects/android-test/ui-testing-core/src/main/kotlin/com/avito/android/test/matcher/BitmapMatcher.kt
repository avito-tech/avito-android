package com.avito.android.test.matcher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.test.espresso.matcher.BoundedMatcher
import com.avito.android.test.util.toBitmap
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import kotlin.math.absoluteValue

class BitmapMatcher(private val expectedBitmap: Bitmap) :
    BoundedMatcher<View, ImageView>(ImageView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with bitmap: ")
    }

    override fun matchesSafely(view: ImageView): Boolean {
        val actualBitmap: Bitmap = when (val drawable = view.drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> drawable.toBitmap()
        }
        if (actualBitmap.height != expectedBitmap.height
            || actualBitmap.width != expectedBitmap.width
        ) {
            throw AssertionFailedError(
                "Bitmaps has different sizes: " +
                    "actual [${actualBitmap.height}x${actualBitmap.width}], " +
                    "compared [${expectedBitmap.height}x${expectedBitmap.width}]"
            )
        }
        actualBitmap.comparePixels(expectedBitmap) { x, y, leftColor, rightColor ->
            if (!hasSameColor(leftColor, rightColor)) {
                throw AssertionFailedError(
                    "Bitmaps are different. " +
                        "Pixel at [$x,$y]: " +
                        "actual=${hexColor(leftColor)}, " +
                        "expected=${hexColor(rightColor)}"
                )
            }
        }
        return true
    }

    private fun Bitmap.comparePixels(
        other: Bitmap,
        comparator: (x: Int, y: Int, /* @ColorInt */ left: Int, /* @ColorInt */ right: Int) -> Unit
    ) {
        require(this.height == other.height)
        require(this.width == other.width)

        for (y in 0 until this.height) {
            for (x in 0 until this.width) {
                val leftColor = getPixel(x, y)
                val rightColor = other.getPixel(x, y)
                comparator(x, y, leftColor, rightColor)
            }
        }
    }

    @Suppress("CustomColorsKotlin")
    private fun hasSameColor(@ColorInt pixel1: Int, @ColorInt pixel2: Int): Boolean {
        val r = (Color.red(pixel1) - Color.red(pixel2)).absoluteValue
        val g = (Color.green(pixel1) - Color.green(pixel2)).absoluteValue
        val b = (Color.blue(pixel1) - Color.blue(pixel2)).absoluteValue
        // As we do some on-device-compression there is confidence interval
        return r < CONFIDENCE_INTERVAL && g < CONFIDENCE_INTERVAL && b < CONFIDENCE_INTERVAL
    }

    private fun hexColor(@ColorInt color: Int) = String.format("#%08X", color)
}

private const val CONFIDENCE_INTERVAL = 10
