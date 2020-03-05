package com.avito.android.rule

import com.avito.android.util.PlatformProxyToast
import com.avito.android.util.ProxyToast

class ToastRule : SimpleRule() {

    private val mockProxyToast: MockProxyToast = MockProxyToast(original = PlatformProxyToast())

    val checks: ToastChecks = ProxyToastChecks(mockProxyToast)

    override fun before() {
        mockProxyToast.clear()
        ProxyToast.instance = mockProxyToast
    }

    fun clearRecordedInvocations() {
        mockProxyToast.clear()
    }
}
