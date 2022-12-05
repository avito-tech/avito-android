package com.avito.android.diff_util.sequence

import com.avito.android.diff_util.Sequence

public class StringSequence(private val items: List<String>) : Sequence() {

    override fun size(): Int = items.size

    public operator fun get(index: Int): String = items[index]
}

public fun List<String>.toSequence(): StringSequence = StringSequence(this)
