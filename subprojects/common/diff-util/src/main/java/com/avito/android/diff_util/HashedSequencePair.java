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
 * Wraps two {@link com.avito.android.diff_util.Sequence} instances to cache their
 * element hash codes.
 * <p>
 * This pair wraps two sequences that contain cached hash codes for the input
 * sequences.
 *
 * @param <S>
 *            the base sequence type.
 */
public class HashedSequencePair<S extends Sequence> {
    private final SequenceComparator<? super S> cmp;
    private final S baseA;
    private final S baseB;
    private HashedSequence<S> cachedA;
    private HashedSequence<S> cachedB;
    /**
     * Construct a pair to provide fast hash codes.
     *
     * @param cmp
     *            the base comparator for the sequence elements.
     * @param a
     *            the A sequence.
     * @param b
     *            the B sequence.
     */
    public HashedSequencePair(SequenceComparator<? super S> cmp, S a, S b) {
        this.cmp = cmp;
        this.baseA = a;
        this.baseB = b;
    }
    /**
     * Get comparator
     *
     * @return obtain a comparator that uses the cached hash codes
     */
    public HashedSequenceComparator<S> getComparator() {
        return new HashedSequenceComparator<>(cmp);
    }
    /**
     * Get A
     *
     * @return wrapper around A that includes cached hash codes
     */
    public HashedSequence<S> getA() {
        if (cachedA == null)
            cachedA = wrap(baseA);
        return cachedA;
    }
    /**
     * Get B
     *
     * @return wrapper around B that includes cached hash codes
     */
    public HashedSequence<S> getB() {
        if (cachedB == null)
            cachedB = wrap(baseB);
        return cachedB;
    }
    private HashedSequence<S> wrap(S base) {
        final int end = base.size();
        final int[] hashes = new int[end];
        for (int ptr = 0; ptr < end; ptr++)
            hashes[ptr] = cmp.hash(base, ptr);
        return new HashedSequence<>(base, hashes);
    }
}
