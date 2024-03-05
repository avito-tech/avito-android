package com.avito.android.rule

import com.avito.android.rule.internal.MockProxyToast
import com.avito.android.rule.internal.ProxyToastChecks
import com.avito.android.util.PlatformProxyToast
import com.avito.android.util.ProxyToast

public class ToastRule : SimpleRule() {

    private val mockProxyToast: MockProxyToast = MockProxyToast(original = PlatformProxyToast())

    public val checks: ToastChecks = ProxyToastChecks.create(mockProxyToast)

    override fun before() {
        clearRecordedInvocations()
        ProxyToast.instance = mockProxyToast
    }

    public fun clearRecordedInvocations() {
        mockProxyToast.clear()
    }
}
