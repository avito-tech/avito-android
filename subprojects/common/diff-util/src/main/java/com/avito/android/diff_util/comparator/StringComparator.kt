package com.avito.android.diff_util.comparator

import com.avito.android.diff_util.SequenceComparator
import com.avito.android.diff_util.sequence.StringSequence

public class StringComparator : SequenceComparator<StringSequence>() {
    override fun equals(a: StringSequence, ai: Int, b: StringSequence, bi: Int): Boolean {
        return a[ai] == b[bi]
    }

    override fun hash(seq: StringSequence, ptr: Int): Int {
        return seq[ptr].hashCode()
    }
}
