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
 * Compares two {@link com.avito.android.diff_util.Sequence}s to create an
 * {@link com.avito.android.diff_util.EditList} of changes.
 * <p>
 * An algorithm's {@code diff} method must be callable from concurrent threads
 * without data collisions. This permits some algorithms to use a singleton
 * pattern, with concurrent invocations using the same singleton. Other
 * algorithms may support parameterization, in which case the caller can create
 * a unique instance per thread.
 */
public abstract class DiffAlgorithm {
    /**
     * Supported diff algorithm
     */
    public enum SupportedAlgorithm {
        // deleted by avito
        /*
        MYERS,
        */
        /**
         * Histogram diff algorithm
         */
        HISTOGRAM
    }
    /**
     * Get diff algorithm
     *
     * @param alg
     *            the diff algorithm for which an implementation should be
     *            returned
     * @return an implementation of the specified diff algorithm
     */
    public static DiffAlgorithm getAlgorithm(SupportedAlgorithm alg) {
        if (alg == SupportedAlgorithm.HISTOGRAM) {
            return new HistogramDiff();
        }
        throw new IllegalArgumentException();
    }
    /**
     * Compare two sequences and identify a list of edits between them.
     *
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
     * @return a modifiable edit list comparing the two sequences. If empty, the
     *         sequences are identical according to {@code cmp}'s rules. The
     *         result list is never null.
     */
    public <S extends Sequence> EditList diff(
        SequenceComparator<? super S> cmp, S a, S b) {
        Edit region = cmp.reduceCommonStartEnd(a, b, coverEdit(a, b));
        switch (region.getType()) {
            case INSERT:
            case DELETE:
                return EditList.singleton(region);
            case REPLACE: {
                if (region.getLengthA() == 1 && region.getLengthB() == 1)
                    return EditList.singleton(region);
                SubsequenceComparator<S> cs = new SubsequenceComparator<>(cmp);
                Subsequence<S> as = Subsequence.a(a, region);
                Subsequence<S> bs = Subsequence.b(b, region);
                EditList e = Subsequence.toBase(diffNonCommon(cs, as, bs), as, bs);
                return normalize(cmp, e, a, b);
            }
            case EMPTY:
                return new EditList(0);
            default:
                throw new IllegalStateException();
        }
    }
    private static <S extends Sequence> Edit coverEdit(S a, S b) {
        return new Edit(0, a.size(), 0, b.size());
    }
    /**
     * Reorganize an {@link EditList} for better diff consistency.
     * <p>
     * {@code DiffAlgorithms} may return {@link Edit.Type#INSERT} or
     * {@link Edit.Type#DELETE} edits that can be "shifted". For
     * example, the deleted section
     * <pre>
     * -a
     * -b
     * -c
     *  a
     *  b
     *  c
     * </pre>
     * can be shifted down by 1, 2 or 3 locations.
     * <p>
     * To avoid later merge issues, we shift such edits to a
     * consistent location. {@code normalize} uses a simple strategy of
     * shifting such edits to their latest possible location.
     * <p>
     * This strategy may not always produce an aesthetically pleasing
     * diff. For instance, it works well with
     * <pre>
     *  function1 {
     *   ...
     *  }
     *
     * +function2 {
     * + ...
     * +}
     * +
     * function3 {
     * ...
     * }
     * </pre>
     * but less so for
     * <pre>
     *  #
     *  # comment1
     *  #
     *  function1() {
     *  }
     *
     *  #
     * +# comment3
     * +#
     * +function3() {
     * +}
     * +
     * +#
     *  # comment2
     *  #
     *  function2() {
     *  }
     * </pre>
     * <a href="https://github.com/mhagger/diff-slider-tools">More
     * sophisticated strategies</a> are possible, say by calculating a
     * suitable "aesthetic cost" for each possible position and using
     * the lowest cost, but {@code normalize} just shifts edits
     * to the end as much as possible.
     *
     * @param <S>
     *            type of sequence being compared.
     * @param cmp
     *            the comparator supplying the element equivalence function.
     * @param e
     *            a modifiable edit list comparing the provided sequences.
     * @param a
     *            the first (also known as old or pre-image) sequence.
     * @param b
     *            the second (also known as new or post-image) sequence.
     * @return a modifiable edit list with edit regions shifted to their
     *         latest possible location. The result list is never null.
     * @since 4.7
     */
    private static <S extends Sequence> EditList normalize(
        SequenceComparator<? super S> cmp, EditList e, S a, S b) {
        Edit prev = null;
        for (int i = e.size() - 1; i >= 0; i--) {
            Edit cur = e.get(i);
            Edit.Type curType = cur.getType();
            int maxA = (prev == null) ? a.size() : prev.beginA;
            int maxB = (prev == null) ? b.size() : prev.beginB;
            if (curType == Edit.Type.INSERT) {
                while (cur.endA < maxA && cur.endB < maxB
                    && cmp.equals(b, cur.beginB, b, cur.endB)) {
                    cur.shift(1);
                }
            } else if (curType == Edit.Type.DELETE) {
                while (cur.endA < maxA && cur.endB < maxB
                    && cmp.equals(a, cur.beginA, a, cur.endA)) {
                    cur.shift(1);
                }
            }
            prev = cur;
        }
        return e;
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
     * @return a modifiable edit list comparing the two sequences.
     */
    public abstract <S extends Sequence> EditList diffNonCommon(
        SequenceComparator<? super S> cmp, S a, S b);
}
