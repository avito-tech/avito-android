package com.avito.android.gradle.profile

import java.util.Comparator

public abstract class Operation {

    public abstract val elapsedTime: Long

    public abstract val description: String

    public companion object {

        public fun slowestFirst(): Comparator<in Operation> {
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
