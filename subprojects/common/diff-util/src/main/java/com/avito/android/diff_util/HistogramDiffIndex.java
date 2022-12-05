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
 * Support {@link HistogramDiff} by computing occurrence counts of elements.
 * <p>
 * Each element in the range being considered is put into a hash table, tracking
 * the number of times that distinct element appears in the sequence. Once all
 * elements have been inserted from sequence A, each element of sequence B is
 * probed in the hash table and the longest common subsequence with the lowest
 * occurrence count in A is used as the result.
 *
 * @param <S>
 *            type of the base sequence.
 */
final class HistogramDiffIndex<S extends Sequence> {
    private static final int REC_NEXT_SHIFT = 28 + 8;
    private static final int REC_PTR_SHIFT = 8;
    private static final int REC_PTR_MASK = (1 << 28) - 1;
    private static final int REC_CNT_MASK = (1 << 8) - 1;
    private static final int MAX_PTR = REC_PTR_MASK;
    private static final int MAX_CNT = (1 << 8) - 1;
    private final int maxChainLength;
    private final HashedSequenceComparator<S> cmp;
    private final HashedSequence<S> a;
    private final HashedSequence<S> b;
    private final Edit region;
    /** Keyed by {@link #hash(HashedSequence, int)} for {@link #recs} index. */
    private final int[] table;
    /** Number of low bits to discard from a key to index {@link #table}. */
    private final int keyShift;
    /**
     * Describes a unique element in sequence A.
     *
     * The records in this table are actually 3-tuples of:
     * <ul>
     * <li>index of next record in this table that has same hash code</li>
     * <li>index of first element in this occurrence chain</li>
     * <li>occurrence count for this element (length of locs list)</li>
     * </ul>
     *
     * The occurrence count is capped at {@link #MAX_CNT}, as the field is only
     * a few bits wide. Elements that occur more frequently will have their
     * count capped.
     */
    private long[] recs;
    /** Number of elements in {@link #recs}; also is the unique element count. */
    private int recCnt;
    /**
     * For {@code ptr}, {@code next[ptr - ptrShift]} has subsequent index.
     *
     * For the sequence element {@code ptr}, the value stored at location
     * {@code next[ptr - ptrShift]} is the next occurrence of the exact same
     * element in the sequence.
     *
     * Chains always run from the lowest index to the largest index. Therefore
     * the array will store {@code next[1] = 2}, but never {@code next[2] = 1}.
     * This allows a chain to terminate with {@code 0}, as {@code 0} would never
     * be a valid next element.
     *
     * The array is sized to be {@code region.getLengthA()} and element indexes
     * are converted to array indexes by subtracting {@link #ptrShift}, which is
     * just a cached version of {@code region.beginA}.
     */
    private int[] next;
    /**
     * For element {@code ptr} in A, index of the record in {@link #recs} array.
     *
     * The record at {@code recs[recIdx[ptr - ptrShift]]} is the record
     * describing all occurrences of the element appearing in sequence A at
     * position {@code ptr}. The record is needed to get the occurrence count of
     * the element, or to locate all other occurrences of that element within
     * sequence A. This index provides constant-time access to the record, and
     * avoids needing to scan the hash chain.
     */
    private int[] recIdx;
    /** Value to subtract from element indexes to key {@link #next} array. */
    private int ptrShift;
    private Edit lcs;
    private int cnt;
    private boolean hasCommon;
    HistogramDiffIndex(int maxChainLength, HashedSequenceComparator<S> cmp,
                       HashedSequence<S> a, HashedSequence<S> b, Edit r) {
        this.maxChainLength = maxChainLength;
        this.cmp = cmp;
        this.a = a;
        this.b = b;
        this.region = r;
        if (region.endA >= MAX_PTR)
            // changed by avito
            /*
            throw new IllegalArgumentException(
                JGitText.get().sequenceTooLargeForDiffAlgorithm);
            */
            throw new IllegalStateException("Sequence too large for difference algorithm.");
        final int sz = r.getLengthA();
        final int tableBits = tableBits(sz);
        table = new int[1 << tableBits];
        keyShift = 32 - tableBits;
        ptrShift = r.beginA;
        recs = new long[Math.max(4, sz >>> 3)];
        next = new int[sz];
        recIdx = new int[sz];
    }
    Edit findLongestCommonSequence() {
        if (!scanA())
            return null;
        lcs = new Edit(0, 0);
        cnt = maxChainLength + 1;
        for (int bPtr = region.beginB; bPtr < region.endB;)
            bPtr = tryLongestCommonSequence(bPtr);
        return hasCommon && maxChainLength < cnt ? null : lcs;
    }
    private boolean scanA() {
        // Scan the elements backwards, inserting them into the hash table
        // as we go. Going in reverse places the earliest occurrence of any
        // element at the start of the chain, so we consider earlier matches
        // before later matches.
        //
        SCAN: for (int ptr = region.endA - 1; region.beginA <= ptr; ptr--) {
            final int tIdx = hash(a, ptr);
            int chainLen = 0;
            for (int rIdx = table[tIdx]; rIdx != 0;) {
                final long rec = recs[rIdx];
                if (cmp.equals(a, recPtr(rec), a, ptr)) {
                    // ptr is identical to another element. Insert it onto
                    // the front of the existing element chain.
                    //
                    int newCnt = recCnt(rec) + 1;
                    if (MAX_CNT < newCnt)
                        newCnt = MAX_CNT;
                    recs[rIdx] = recCreate(recNext(rec), ptr, newCnt);
                    next[ptr - ptrShift] = recPtr(rec);
                    recIdx[ptr - ptrShift] = rIdx;
                    continue SCAN;
                }
                rIdx = recNext(rec);
                chainLen++;
            }
            if (chainLen == maxChainLength)
                return false;
            // This is the first time we have ever seen this particular
            // element in the sequence. Construct a new chain for it.
            //
            final int rIdx = ++recCnt;
            if (rIdx == recs.length) {
                int sz = Math.min(recs.length << 1, 1 + region.getLengthA());
                long[] n = new long[sz];
                System.arraycopy(recs, 0, n, 0, recs.length);
                recs = n;
            }
            recs[rIdx] = recCreate(table[tIdx], ptr, 1);
            recIdx[ptr - ptrShift] = rIdx;
            table[tIdx] = rIdx;
        }
        return true;
    }
    private int tryLongestCommonSequence(int bPtr) {
        int bNext = bPtr + 1;
        int rIdx = table[hash(b, bPtr)];
        for (long rec; rIdx != 0; rIdx = recNext(rec)) {
            rec = recs[rIdx];
            // If there are more occurrences in A, don't use this chain.
            if (recCnt(rec) > cnt) {
                if (!hasCommon)
                    hasCommon = cmp.equals(a, recPtr(rec), b, bPtr);
                continue;
            }
            int as = recPtr(rec);
            if (!cmp.equals(a, as, b, bPtr))
                continue;
            hasCommon = true;
            TRY_LOCATIONS: for (;;) {
                int np = next[as - ptrShift];
                int bs = bPtr;
                int ae = as + 1;
                int be = bs + 1;
                int rc = recCnt(rec);
                while (region.beginA < as && region.beginB < bs
                    && cmp.equals(a, as - 1, b, bs - 1)) {
                    as--;
                    bs--;
                    if (1 < rc)
                        rc = Math.min(rc, recCnt(recs[recIdx[as - ptrShift]]));
                }
                while (ae < region.endA && be < region.endB
                    && cmp.equals(a, ae, b, be)) {
                    if (1 < rc)
                        rc = Math.min(rc, recCnt(recs[recIdx[ae - ptrShift]]));
                    ae++;
                    be++;
                }
                if (bNext < be)
                    bNext = be;
                if (lcs.getLengthA() < ae - as || rc < cnt) {
                    // If this region is the longest, or there are less
                    // occurrences of it in A, its now our LCS.
                    //
                    lcs.beginA = as;
                    lcs.beginB = bs;
                    lcs.endA = ae;
                    lcs.endB = be;
                    cnt = rc;
                }
                // Because we added elements in reverse order index 0
                // cannot possibly be the next position. Its the first
                // element of the sequence and thus would have been the
                // value of as at the start of the TRY_LOCATIONS loop.
                //
                if (np == 0)
                    break TRY_LOCATIONS;
                while (np < ae) {
                    // The next location to consider was actually within
                    // the LCS we examined above. Don't reconsider it.
                    //
                    np = next[np - ptrShift];
                    if (np == 0)
                        break TRY_LOCATIONS;
                }
                as = np;
            }
        }
        return bNext;
    }
    private int hash(HashedSequence<S> s, int idx) {
        return (cmp.hash(s, idx) * 0x9e370001 /* mix bits */) >>> keyShift;
    }
    private static long recCreate(int next, int ptr, int cnt) {
        return ((long) next << REC_NEXT_SHIFT) //
            | ((long) ptr << REC_PTR_SHIFT) //
            | cnt;
    }
    private static int recNext(long rec) {
        return (int) (rec >>> REC_NEXT_SHIFT);
    }
    private static int recPtr(long rec) {
        return ((int) (rec >>> REC_PTR_SHIFT)) & REC_PTR_MASK;
    }
    private static int recCnt(long rec) {
        return ((int) rec) & REC_CNT_MASK;
    }
    private static int tableBits(int sz) {
        int bits = 31 - Integer.numberOfLeadingZeros(sz);
        if (bits == 0)
            bits = 1;
        if (1 << bits < sz)
            bits++;
        return bits;
    }
}
