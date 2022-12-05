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
 * Wrap another comparator for use with
 * {@link com.avito.android.diff_util.Subsequence}.
 * <p>
 * This comparator acts as a proxy for the real comparator, translating element
 * indexes on the fly by adding the subsequence's begin offset to them.
 * Comparators of this type must be used with a
 * {@link com.avito.android.diff_util.Subsequence}.
 *
 * @param <S>
 *            the base sequence type.
 */
public final class SubsequenceComparator<S extends Sequence> extends
    SequenceComparator<Subsequence<S>> {
    private final SequenceComparator<? super S> cmp;
    /**
     * Construct a comparator wrapping another comparator.
     *
     * @param cmp
     *            the real comparator.
     */
    public SubsequenceComparator(SequenceComparator<? super S> cmp) {
        this.cmp = cmp;
    }
    /** {@inheritDoc} */
    @Override
    public boolean equals(Subsequence<S> a, int ai, Subsequence<S> b, int bi) {
        return cmp.equals(a.base, ai + a.begin, b.base, bi + b.begin);
    }
    /** {@inheritDoc} */
    @Override
    public int hash(Subsequence<S> seq, int ptr) {
        return cmp.hash(seq.base, ptr + seq.begin);
    }
}
