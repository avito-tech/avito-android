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
 * Compares two sequences primarily based upon hash codes.
 */
public abstract class LowLevelDiffAlgorithm extends DiffAlgorithm {
    /** {@inheritDoc} */
    @Override
    public <S extends Sequence> EditList diffNonCommon(
        SequenceComparator<? super S> cmp, S a, S b) {
        HashedSequencePair<S> p = new HashedSequencePair<>(cmp, a, b);
        HashedSequenceComparator<S> hc = p.getComparator();
        HashedSequence<S> ha = p.getA();
        HashedSequence<S> hb = p.getB();
        p = null;
        EditList res = new EditList();
        Edit region = new Edit(0, a.size(), 0, b.size());
        diffNonCommon(res, hc, ha, hb, region);
        return res;
    }
    /**
     * Compare two sequences and identify a list of edits between them.
     *
     * This method should be invoked only after the two sequences have been
     * proven to have no common starting or ending elements. The expected
     * elimination of common starting and ending elements is automatically
     * performed by the {@link #diff(SequenceComparator, Sequence, Sequence)}
     * method, which invokes this method using
     * {@link com.avito.android.diff_util.Subsequence}s.
     *
     * @param edits
     *            result list to append the region's edits onto.
     * @param cmp
     *            the comparator supplying the element equivalence function.
     * @param a
     *            the first (also known as old or pre-image) sequence. Edits
     *            returned by this algorithm will reference indexes using the
     *            'A' side: {@link com.avito.android.diff_util.Edit#getBeginA()},
     *            {@link com.avito.android.diff_util.Edit#getEndA()}.
     * @param b
     *            the second (also known as new or post-image) sequence. Edits
     *            returned by this algorithm will reference indexes using the
     *            'B' side: {@link com.avito.android.diff_util.Edit#getBeginB()},
     *            {@link com.avito.android.diff_util.Edit#getEndB()}.
     * @param region
     *            the region being compared within the two sequences.
     */
    public abstract <S extends Sequence> void diffNonCommon(EditList edits,
                                                            HashedSequenceComparator<S> cmp, HashedSequence<S> a,
                                                            HashedSequence<S> b, Edit region);
}
