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
 * {@link com.avito.android.diff_util.HashedSequence}.
 * <p>
 * This comparator acts as a proxy for the real comparator, evaluating the
 * cached hash code before testing the underlying comparator's equality.
 * Comparators of this type must be used with a
 * {@link com.avito.android.diff_util.HashedSequence}.
 * <p>
 * To construct an instance of this type use
 * {@link com.avito.android.diff_util.HashedSequencePair}.
 *
 * @param <S>
 *            the base sequence type.
 */
public final class HashedSequenceComparator<S extends Sequence> extends
    SequenceComparator<HashedSequence<S>> {
    private final SequenceComparator<? super S> cmp;
    HashedSequenceComparator(SequenceComparator<? super S> cmp) {
        this.cmp = cmp;
    }
    /** {@inheritDoc} */
    @Override
    public boolean equals(HashedSequence<S> a, int ai, //
                          HashedSequence<S> b, int bi) {
        return a.hashes[ai] == b.hashes[bi]
            && cmp.equals(a.base, ai, b.base, bi);
    }
    /** {@inheritDoc} */
    @Override
    public int hash(HashedSequence<S> seq, int ptr) {
        return seq.hashes[ptr];
    }
}
