package com.avito.android.gradle.profile

import java.util.Comparator

abstract class Operation {

    abstract val elapsedTime: Long

    abstract val description: String

    companion object {

        fun slowestFirst(): Comparator<in Operation> {
            return Comparator { o1, o2 ->
                val byElapsedTime = o2.elapsedTime - o1.elapsedTime
                if (byElapsedTime > 0L) {
                    1
                } else {
                    if (byElapsedTime < 0L) -1 else o1.description.compareTo(o2.description)
                }
            }
        }
    }
}
