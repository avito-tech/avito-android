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

import java.util.ArrayList;
import java.util.List;

/**
 * An extended form of Bram Cohen's patience diff algorithm.
 * <p>
 * This implementation was derived by using the 4 rules that are outlined in
 * Bram Cohen's <a href="http://bramcohen.livejournal.com/73318.html">blog</a>,
 * and then was further extended to support low-occurrence common elements.
 * <p>
 * The basic idea of the algorithm is to create a histogram of occurrences for
 * each element of sequence A. Each element of sequence B is then considered in
 * turn. If the element also exists in sequence A, and has a lower occurrence
 * count, the positions are considered as a candidate for the longest common
 * subsequence (LCS). After scanning of B is complete the LCS that has the
 * lowest number of occurrences is chosen as a split point. The region is split
 * around the LCS, and the algorithm is recursively applied to the sections
 * before and after the LCS.
 * <p>
 * By always selecting a LCS position with the lowest occurrence count, this
 * algorithm behaves exactly like Bram Cohen's patience diff whenever there is a
 * unique common element available between the two sequences. When no unique
 * elements exist, the lowest occurrence element is chosen instead. This offers
 * more readable diffs than simply falling back on the standard Myers' O(ND)
 * algorithm would produce.
 * <p>
 * To prevent the algorithm from having an O(N^2) running time, an upper limit
 * on the number of unique elements in a histogram bucket is configured by
 * {@link #setMaxChainLength(int)}. If sequence A has more than this many
 * elements that hash into the same hash bucket, the algorithm passes the region
 * to {@link #setFallbackAlgorithm(DiffAlgorithm)}. If no fallback algorithm is
 * configured, the region is emitted as a replace edit.
 * <p>
 * During scanning of sequence B, any element of A that occurs more than
 * {@link #setMaxChainLength(int)} times is never considered for an LCS match
 * position, even if it is common between the two sequences. This limits the
 * number of locations in sequence A that must be considered to find the LCS,
 * and helps maintain a lower running time bound.
 * <p>
 * So long as {@link #setMaxChainLength(int)} is a small constant (such as 64),
 * the algorithm runs in O(N * D) time, where N is the sum of the input lengths
 * and D is the number of edits in the resulting EditList. If the supplied
 * {@link com.avito.android.diff_util.SequenceComparator} has a good hash function,
 * this implementation typically out-performs MyersDiff, even though its theoretical running
 * time is the same.
 * <p>
 * This implementation has an internal limitation that prevents it from handling
 * sequences with more than 268,435,456 (2^28) elements.
 */
public class HistogramDiff extends LowLevelDiffAlgorithm {
    /** Algorithm to use when there are too many element occurrences. */
    // changed by avito
    /*
    DiffAlgorithm fallback = MyersDiff.INSTANCE;
    */
    DiffAlgorithm fallback = null;
    /**
     * Maximum number of positions to consider for a given element hash.
     *
     * All elements with the same hash are stored into a single chain. The chain
     * size is capped to ensure search is linear time at O(len_A + len_B) rather
     * than quadratic at O(len_A * len_B).
     */
    int maxChainLength = 64;
    /**
     * Set the algorithm used when there are too many element occurrences.
     *
     * @param alg
     *            the secondary algorithm. If null the region will be denoted as
     *            a single REPLACE block.
     */
    public void setFallbackAlgorithm(DiffAlgorithm alg) {
        fallback = alg;
    }
    /**
     * Maximum number of positions to consider for a given element hash.
     *
     * All elements with the same hash are stored into a single chain. The chain
     * size is capped to ensure search is linear time at O(len_A + len_B) rather
     * than quadratic at O(len_A * len_B).
     *
     * @param maxLen
     *            new maximum length.
     */
    public void setMaxChainLength(int maxLen) {
        maxChainLength = maxLen;
    }
    /** {@inheritDoc} */
    @Override
    public <S extends Sequence> void diffNonCommon(EditList edits,
                                                   HashedSequenceComparator<S> cmp, HashedSequence<S> a,
                                                   HashedSequence<S> b, Edit region) {
        new State<>(edits, cmp, a, b).diffRegion(region);
    }
    private class State<S extends Sequence> {
        private final HashedSequenceComparator<S> cmp;
        private final HashedSequence<S> a;
        private final HashedSequence<S> b;
        private final List<Edit> queue = new ArrayList<>();
        /** Result edits we have determined that must be made to convert a to b. */
        final EditList edits;
        State(EditList edits, HashedSequenceComparator<S> cmp,
              HashedSequence<S> a, HashedSequence<S> b) {
            this.cmp = cmp;
            this.a = a;
            this.b = b;
            this.edits = edits;
        }
        void diffRegion(Edit r) {
            diffReplace(r);
            while (!queue.isEmpty())
                diff(queue.remove(queue.size() - 1));
        }
        private void diffReplace(Edit r) {
            Edit lcs = new HistogramDiffIndex<>(maxChainLength, cmp, a, b, r)
                .findLongestCommonSequence();
            if (lcs != null) {
                // If we were given an edit, we can prove a result here.
                //
                if (lcs.isEmpty()) {
                    // An empty edit indicates there is nothing in common.
                    // Replace the entire region.
                    //
                    edits.add(r);
                } else {
                    queue.add(r.after(lcs));
                    queue.add(r.before(lcs));
                }
            } else if (fallback instanceof LowLevelDiffAlgorithm) {
                LowLevelDiffAlgorithm fb = (LowLevelDiffAlgorithm) fallback;
                fb.diffNonCommon(edits, cmp, a, b, r);
            } else if (fallback != null) {
                SubsequenceComparator<HashedSequence<S>> cs = subcmp();
                Subsequence<HashedSequence<S>> as = Subsequence.a(a, r);
                Subsequence<HashedSequence<S>> bs = Subsequence.b(b, r);
                EditList res = fallback.diffNonCommon(cs, as, bs);
                edits.addAll(Subsequence.toBase(res, as, bs));
            } else {
                edits.add(r);
            }
        }
        private void diff(Edit r) {
            switch (r.getType()) {
                case INSERT:
                case DELETE:
                    edits.add(r);
                    break;
                case REPLACE:
                    if (r.getLengthA() == 1 && r.getLengthB() == 1)
                        edits.add(r);
                    else
                        diffReplace(r);
                    break;
                case EMPTY:
                default:
                    throw new IllegalStateException();
            }
        }
        private SubsequenceComparator<HashedSequence<S>> subcmp() {
            return new SubsequenceComparator<>(cmp);
        }
    }
}
