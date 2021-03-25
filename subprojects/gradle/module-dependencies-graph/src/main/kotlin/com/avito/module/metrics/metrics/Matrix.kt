package com.avito.module.metrics.metrics

internal class Matrix<COORDINATE, VALUE> {

    private val data = mutableMapOf<COORDINATE, MutableMap<COORDINATE, VALUE>>()

    fun rowsCoordinates(): Set<COORDINATE> {
        return data.keys
    }

    fun columnsCoordinates(): Set<COORDINATE> {
        return rowsCoordinates().flatMap { row(it).keys }.toSet()
    }

    fun getOrNull(row: COORDINATE, column: COORDINATE): VALUE? {
        return row(row)[column]
    }

    fun putIfAbsent(row: COORDINATE, column: COORDINATE, value: () -> VALUE) {
        if (row(row)[column] == null) {
            row(row)[column] = value()
        }
    }

    private fun row(row: COORDINATE): MutableMap<COORDINATE, VALUE> =
        data.getOrPut(row) { mutableMapOf() }
}
