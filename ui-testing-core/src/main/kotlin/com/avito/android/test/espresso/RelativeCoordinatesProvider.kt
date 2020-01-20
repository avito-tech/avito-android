package com.avito.android.test.espresso

import android.view.View
import androidx.test.espresso.action.CoordinatesProvider

internal class RelativeCoordinatesProvider(
    private val coordinatesProvider: CoordinatesProvider
) : CoordinatesProvider {
    override fun calculateCoordinates(view: View): FloatArray {
        val rootView = view.rootView
        val rootViewAbsoluteCoordinates = IntArray(2)
        rootView.getLocationOnScreen(rootViewAbsoluteCoordinates)

        val absoluteCoordinates = coordinatesProvider.calculateCoordinates(view)
        val relativeCoordinates = FloatArray(2).apply {
            set(0, absoluteCoordinates[0] - rootViewAbsoluteCoordinates[0])
            set(1, absoluteCoordinates[1] - rootViewAbsoluteCoordinates[1])
        }
        return relativeCoordinates
    }

}
