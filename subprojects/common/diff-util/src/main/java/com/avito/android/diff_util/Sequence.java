/*
 * Copyright (C) 2010, Google Inc.
 * Copyright (C) 2008-2009, Johannes E. Schindelin <johannes.schindelin@gmx.de> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.avito.android.diff_util;

/**
 * Arbitrary sequence of elements.
 * <p>
 * A sequence of elements is defined to contain elements in the index range
 * <code>[0, {@link #size()})</code>, like a standard Java List implementation.
 * Unlike a List, the members of the sequence are not directly obtainable.
 * <p>
 * Implementations of Sequence are primarily intended for use in content
 * difference detection algorithms, to produce an
 * {@link com.avito.android.diff_util.EditList} of {@link com.avito.android.diff_util.Edit}
 * instances describing how two Sequence instances differ.
 * <p>
 * To be compared against another Sequence of the same type, a supporting
 * {@link com.avito.android.diff_util.SequenceComparator} must also be supplied.
 */
public abstract class Sequence {
    /** @return total number of items in the sequence. */
    /**
     * Get size
     *
     * @return size
     */
    public abstract int size();
}
