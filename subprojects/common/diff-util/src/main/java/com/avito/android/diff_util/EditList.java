/*
 * Copyright (C) 2009, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.avito.android.diff_util;

import java.util.ArrayList;

/**
 * Specialized list of {@link com.avito.android.diff_util.Edit}s in a document.
 */
public class EditList extends ArrayList<Edit> {
    private static final long serialVersionUID = 1L;
    /**
     * Construct an edit list containing a single edit.
     *
     * @param edit
     *            the edit to return in the list.
     * @return list containing only {@code edit}.
     */
    public static EditList singleton(Edit edit) {
        EditList res = new EditList(1);
        res.add(edit);
        return res;
    }
    /**
     * Create a new, empty edit list.
     */
    public EditList() {
        super(16);
    }
    /**
     * Create an empty edit list with the specified capacity.
     *
     * @param capacity
     *            the initial capacity of the edit list. If additional edits are
     *            added to the list, it will be grown to support them.
     */
    public EditList(int capacity) {
        super(capacity);
    }
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "EditList" + super.toString(); //$NON-NLS-1$
    }
}
