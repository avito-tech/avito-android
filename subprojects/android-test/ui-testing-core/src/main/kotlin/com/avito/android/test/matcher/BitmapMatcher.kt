package com.avito.android.test.matcher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import com.avito.android.test.util.toBitmap
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import kotlin.math.absoluteValue

class BitmapMatcher(private val bitmap: Bitmap) :
    BoundedMatcher<View, ImageView>(ImageView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with bitmap: ")
    }

    override fun matchesSafely(view: ImageView): Boolean {
        val actualBitmap: Bitmap = when (val drawable = view.drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> drawable.toBitmap()
        }
        if (actualBitmap.height != bitmap.height
            || actualBitmap.width != bitmap.width
        ) {
            throw AssertionFailedError(
                "Bitmaps has different sizes: " +
                    "actual [${actualBitmap.height}x${actualBitmap.width}], compared [${bitmap.height}x${bitmap.width}]"
            )
        }
        val actualPixels = actualBitmap.getPixelsSnapshot()
        val shouldBePixels = bitmap.getPixelsSnapshot()

        shouldBePixels.forEachIndexed { index, shouldBePixel ->
            val actualPixel = actualPixels[index]
            if (!hasSameColor(shouldBePixel, actualPixel)) {
                throw AssertionFailedError("Bitmaps are different")
            }
        }
        return true
    }

    @Suppress("CustomColorsKotlin")
    private fun hasSameColor(pixel1: Int, pixel2: Int): Boolean {
        val r = (Color.red(pixel1) - Color.red(pixel2)).absoluteValue
        val g = (Color.green(pixel1) - Color.green(pixel2)).absoluteValue
        val b = (Color.blue(pixel1) - Color.blue(pixel2)).absoluteValue
        // As we do some on-device-compression there is confidence interval
        return r < CONFIDENCE_INTERVAL && g < CONFIDENCE_INTERVAL && b < CONFIDENCE_INTERVAL
    }

    private fun Bitmap.getPixelsSnapshot(): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 1 until this.width step 5) {
            for (j in 1 until this.height step 5) {
                result.add(getPixel(i, j))
            }
        }
        return result
    }
}

private const val CONFIDENCE_INTERVAL = 10
