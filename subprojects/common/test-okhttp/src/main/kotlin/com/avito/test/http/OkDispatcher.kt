package com.avito.test.http

import okhttp3.mockwebserver.MockResponse

object OkDispatcher : ConstantResponseDispatcher(MockResponse().setResponseCode(200))
