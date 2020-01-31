package com.avito.android.test.maps

import com.google.android.gms.maps.model.LatLng
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import kotlin.math.abs

fun hasTheSameCoordinatesAs(expected: LatLng): Matcher<LatLng> = object : BaseMatcher<LatLng>() {

    override fun describeTo(description: Description) {
        description.appendText("$expected (comparing with $COMPARING_TOLERANCE tolerance)")
    }

    override fun matches(item: Any): Boolean = if (item is LatLng) {
        abs(item.latitude - expected.latitude) < COMPARING_TOLERANCE &&
                abs(item.longitude - expected.longitude) < COMPARING_TOLERANCE
    } else {
        false
    }
}

private const val COMPARING_TOLERANCE = 0.0001
