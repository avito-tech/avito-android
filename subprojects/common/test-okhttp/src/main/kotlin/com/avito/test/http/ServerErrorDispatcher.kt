package com.avito.test.http

import okhttp3.mockwebserver.MockResponse

object ServerErrorDispatcher : ConstantResponseDispatcher(MockResponse().setResponseCode(500))
