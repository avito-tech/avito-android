package com.avito.android.rule

import com.avito.android.rule.internal.MockProxyToast
import com.avito.android.rule.internal.ProxyToastChecks
import com.avito.android.util.PlatformProxyToast
import com.avito.android.util.ProxyToast

class ToastRule : SimpleRule() {

    private val mockProxyToast: MockProxyToast = MockProxyToast(original = PlatformProxyToast())

    val checks: ToastChecks = ProxyToastChecks.create(mockProxyToast)

    override fun before() {
        clearRecordedInvocations()
        ProxyToast.instance = mockProxyToast
    }

    fun clearRecordedInvocations() {
        mockProxyToast.clear()
    }
}
