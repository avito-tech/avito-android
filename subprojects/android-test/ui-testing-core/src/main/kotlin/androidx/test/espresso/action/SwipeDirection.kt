package androidx.test.espresso.action

import androidx.test.espresso.action.GeneralLocation.translate

interface SwipeDirection {
    fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider>
}

enum class SwipeDirections : SwipeDirection {
    LEFT_TO_RIGHT {
        override fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider> {
            return translate(
                GeneralLocation.CENTER_LEFT,
                EDGE_FUZZ_FACTOR,
                0f
            ) to GeneralLocation.CENTER_RIGHT
        }
    },
    RIGHT_TO_LEFT {
        override fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider> {
            return translate(
                GeneralLocation.CENTER_RIGHT,
                -EDGE_FUZZ_FACTOR,
                0f
            ) to GeneralLocation.CENTER_LEFT
        }
    },
    BOTTOM_TO_TOP {
        override fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider> {
            return translate(
                GeneralLocation.BOTTOM_CENTER,
                0f,
                -EDGE_FUZZ_FACTOR
            ) to GeneralLocation.TOP_CENTER
        }
    },
    TOP_TO_BOTTOM {
        override fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider> {
            return translate(
                GeneralLocation.TOP_CENTER,
                0f,
                EDGE_FUZZ_FACTOR
            ) to GeneralLocation.BOTTOM_CENTER
        }
    }
}

/**
 * The distance of a swipe's start position from the view's edge, in terms of the view's length.
 * We do not start the swipe exactly on the view's edge, but somewhat more inward, since swiping
 * from the exact edge may behave in an unexpected way (e.g. may open a navigation drawer).
 */
private const val EDGE_FUZZ_FACTOR = 0.083f
