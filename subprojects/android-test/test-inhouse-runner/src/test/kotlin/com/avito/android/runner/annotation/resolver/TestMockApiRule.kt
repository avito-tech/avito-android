package com.avito.android.runner.annotation.resolver

import com.avito.android.api.AbstractMockApiRule
import com.avito.android.api.RequestRegistry

class TestMockApiRule : AbstractMockApiRule<RequestRegistry>() {

    override fun createRegistry(): RequestRegistry = error("nothing to see here")
}
