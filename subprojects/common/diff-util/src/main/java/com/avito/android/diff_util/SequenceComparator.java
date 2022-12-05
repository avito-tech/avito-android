/*
 * Copyright (C) 2010, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.avito.android.diff_util;

/**
 * Equivalence function for a {@link com.avito.android.diff_util.Sequence} compared by
 * difference algorithm.
 * <p>
 * Difference algorithms can use a comparator to compare portions of two
 * sequences and discover the minimal edits required to transform from one
 * sequence to the other sequence.
 * <p>
 * Indexes within a sequence are zero-based.
 *
 * @param <S>
 *            type of sequence the comparator supports.
 */
public abstract class SequenceComparator<S extends Sequence> {
    /**
     * Compare two items to determine if they are equivalent.
     *
     * It is permissible to compare sequence {@code a} with itself (by passing
     * {@code a} again in position {@code b}).
     *
     * @param a
     *            the first sequence.
     * @param ai
     *            item of {@code ai} to compare.
     * @param b
     *            the second sequence.
     * @param bi
     *            item of {@code bi} to compare.
     * @return true if the two items are identical according to this function's
     *         equivalence rule.
     */
    public abstract boolean equals(S a, int ai, S b, int bi);
    /**
     * Get a hash value for an item in a sequence.
     *
     * If two items are equal according to this comparator's
     * {@link #equals(Sequence, int, Sequence, int)} method, then this hash
     * method must produce the same integer result for both items.
     *
     * It is not required for two items to have different hash values if they
     * are unequal according to the {@code equals()} method.
     *
     * @param seq
     *            the sequence.
     * @param ptr
     *            the item to obtain the hash for.
     * @return hash the hash value.
     */
    public abstract int hash(S seq, int ptr);
    /**
     * Modify the edit to remove common leading and trailing items.
     *
     * The supplied edit {@code e} is reduced in size by moving the beginning A
     * and B points so the edit does not cover any items that are in common
     * between the two sequences. The ending A and B points are also shifted to
     * remove common items from the end of the region.
     *
     * @param a
     *            the first sequence.
     * @param b
     *            the second sequence.
     * @param e
     *            the edit to start with and update.
     * @return {@code e} if it was updated in-place, otherwise a new edit
     *         containing the reduced region.
     */
    public Edit reduceCommonStartEnd(S a, S b, Edit e) {
        // Skip over items that are common at the start.
        //
        while (e.beginA < e.endA && e.beginB < e.endB
            && equals(a, e.beginA, b, e.beginB)) {
            e.beginA++;
            e.beginB++;
        }
        // Skip over items that are common at the end.
        //
        while (e.beginA < e.endA && e.beginB < e.endB
            && equals(a, e.endA - 1, b, e.endB - 1)) {
            e.endA--;
            e.endB--;
        }
        return e;
    }
}
