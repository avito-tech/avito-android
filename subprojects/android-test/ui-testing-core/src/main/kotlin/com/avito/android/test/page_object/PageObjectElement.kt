package com.avito.android.test.page_object

import android.view.View
import com.avito.android.test.action.Actions
import com.avito.android.test.checks.Checks
import org.hamcrest.Matcher

// TODO: hide or deprecate private api
public abstract class PageObjectElement : PageObject(), Actions {
    /**
     * Don't use this matcher directly. Make child interaction context instead.
     */
    public abstract val matcher: Matcher<View>
    public abstract val actions: Actions
    public abstract val checks: Checks
}
