/*
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
 * A modified region detected between two versions of roughly the same content.
 * <p>
 * An edit covers the modified region only. It does not cover a common region.
 * <p>
 * Regions should be specified using 0 based notation, so add 1 to the start and
 * end marks for line numbers in a file.
 * <p>
 * An edit where {@code beginA == endA && beginB < endB} is an insert edit, that
 * is sequence B inserted the elements in region <code>[beginB, endB)</code> at
 * <code>beginA</code>.
 * <p>
 * An edit where {@code beginA < endA && beginB == endB} is a delete edit, that
 * is sequence B has removed the elements between <code>[beginA, endA)</code>.
 * <p>
 * An edit where {@code beginA < endA && beginB < endB} is a replace edit, that
 * is sequence B has replaced the range of elements between
 * <code>[beginA, endA)</code> with those found in <code>[beginB, endB)</code>.
 */
public class Edit {
    /** Type of edit */
    public enum Type {
        /** Sequence B has inserted the region. */
        INSERT,
        /** Sequence B has removed the region. */
        DELETE,
        /** Sequence B has replaced the region with different content. */
        REPLACE,
        /** Sequence A and B have zero length, describing nothing. */
        EMPTY;
    }
    int beginA;
    int endA;
    int beginB;
    int endB;
    /**
     * Create a new empty edit.
     *
     * @param as
     *            beginA: start and end of region in sequence A; 0 based.
     * @param bs
     *            beginB: start and end of region in sequence B; 0 based.
     */
    public Edit(int as, int bs) {
        this(as, as, bs, bs);
    }
    /**
     * Create a new edit.
     *
     * @param as
     *            beginA: start of region in sequence A; 0 based.
     * @param ae
     *            endA: end of region in sequence A; must be &gt;= as.
     * @param bs
     *            beginB: start of region in sequence B; 0 based.
     * @param be
     *            endB: end of region in sequence B; must be &gt; = bs.
     */
    public Edit(int as, int ae, int bs, int be) {
        beginA = as;
        endA = ae;
        beginB = bs;
        endB = be;
    }
    /**
     * Get type
     *
     * @return the type of this region
     */
    public final Type getType() {
        if (beginA < endA) {
            if (beginB < endB) {
                return Type.REPLACE;
            }
            return Type.DELETE;
        }
        if (beginB < endB) {
            return Type.INSERT;
        }
        // beginB == endB)
        return Type.EMPTY;
    }
    /**
     * Whether edit is empty
     *
     * @return {@code true} if the edit is empty (lengths of both a and b is
     *         zero)
     */
    public final boolean isEmpty() {
        return beginA == endA && beginB == endB;
    }
    /**
     * Get start point in sequence A
     *
     * @return start point in sequence A
     */
    public final int getBeginA() {
        return beginA;
    }
    /**
     * Get end point in sequence A
     *
     * @return end point in sequence A
     */
    public final int getEndA() {
        return endA;
    }
    /**
     * Get start point in sequence B
     *
     * @return start point in sequence B
     */
    public final int getBeginB() {
        return beginB;
    }
    /**
     * Get end point in sequence B
     *
     * @return end point in sequence B
     */
    public final int getEndB() {
        return endB;
    }
    /**
     * Get length of the region in A
     *
     * @return length of the region in A
     */
    public final int getLengthA() {
        return endA - beginA;
    }
    /**
     * Get length of the region in B
     *
     * @return return length of the region in B
     */
    public final int getLengthB() {
        return endB - beginB;
    }
    /**
     * Move the edit region by the specified amount.
     *
     * @param amount
     *            the region is shifted by this amount, and can be positive or
     *            negative.
     * @since 4.8
     */
    public final void shift(int amount) {
        beginA += amount;
        endA += amount;
        beginB += amount;
        endB += amount;
    }
    /**
     * Construct a new edit representing the region before cut.
     *
     * @param cut
     *            the cut point. The beginning A and B points are used as the
     *            end points of the returned edit.
     * @return an edit representing the slice of {@code this} edit that occurs
     *         before {@code cut} starts.
     */
    public final Edit before(Edit cut) {
        return new Edit(beginA, cut.beginA, beginB, cut.beginB);
    }
    /**
     * Construct a new edit representing the region after cut.
     *
     * @param cut
     *            the cut point. The ending A and B points are used as the
     *            starting points of the returned edit.
     * @return an edit representing the slice of {@code this} edit that occurs
     *         after {@code cut} ends.
     */
    public final Edit after(Edit cut) {
        return new Edit(cut.endA, endA, cut.endB, endB);
    }
    /**
     * Increase {@link #getEndA()} by 1.
     */
    public void extendA() {
        endA++;
    }
    /**
     * Increase {@link #getEndB()} by 1.
     */
    public void extendB() {
        endB++;
    }
    /**
     * Swap A and B, so the edit goes the other direction.
     */
    public void swap() {
        final int sBegin = beginA;
        final int sEnd = endA;
        beginA = beginB;
        endA = endB;
        beginB = sBegin;
        endB = sEnd;
    }
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return beginA ^ endA;
    }
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Edit) {
            final Edit e = (Edit) o;
            return this.beginA == e.beginA && this.endA == e.endA
                && this.beginB == e.beginB && this.endB == e.endB;
        }
        return false;
    }
    /** {@inheritDoc} */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        final Type t = getType();
        return t + "(" + beginA + "-" + endA + "," + beginB + "-" + endB + ")";
    }
}
