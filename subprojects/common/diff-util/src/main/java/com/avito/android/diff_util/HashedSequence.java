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
 * Wraps a {@link com.avito.android.diff_util.Sequence} to assign hash codes to
 * elements.
 * <p>
 * This sequence acts as a proxy for the real sequence, caching element hash
 * codes so they don't need to be recomputed each time. Sequences of this type
 * must be used with a {@link com.avito.android.diff_util.HashedSequenceComparator}.
 * <p>
 * To construct an instance of this type use
 * {@link com.avito.android.diff_util.HashedSequencePair}.
 *
 * @param <S>
 *            the base sequence type.
 */
public final class HashedSequence<S extends Sequence> extends Sequence {
    final S base;
    final int[] hashes;
    HashedSequence(S base, int[] hashes) {
        this.base = base;
        this.hashes = hashes;
    }
    /** {@inheritDoc} */
    @Override
    public int size() {
        return base.size();
    }
}
