package com.avito.android.test.espresso.action.click

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent

internal fun obtainEvent(coordinates: FloatArray, precision: FloatArray, event: Int) =
    MotionEvent.obtain(
        SystemClock.uptimeMillis(),
        SystemClock.uptimeMillis(),
        event,
        coordinates[0],
        coordinates[1],
        NORMAL_PRESSURE,
        NORMAL_SIZE,
        WITHOUT_MODIFIERS_META_STATE,
        precision[0],
        precision[1],
        InputDevice.SOURCE_UNKNOWN,
        0
    )

internal fun downEvent(coordinates: FloatArray, precision: FloatArray) =
    obtainEvent(coordinates, precision, MotionEvent.ACTION_DOWN)

internal fun upEvent(down: MotionEvent) = MotionEvent.obtain(
    down.downTime,
    SystemClock.uptimeMillis(),
    MotionEvent.ACTION_UP,
    down.x,
    down.y,
    down.pressure,
    down.size,
    down.metaState,
    down.xPrecision,
    down.yPrecision,
    down.deviceId,
    down.edgeFlags
)

private const val NORMAL_PRESSURE = 1F
private const val NORMAL_SIZE = 1F
private const val WITHOUT_MODIFIERS_META_STATE = 0
