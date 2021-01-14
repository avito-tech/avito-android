package com.avito.android.elastic

import okhttp3.ResponseBody
import retrofit2.Call
import java.net.URL

internal class RoundRobinService(
    factory: (endpoint: URL) -> ElasticService,
    endpoints: List<URL>
) : ElasticService {

    private val roundRobin =
        RoundRobinIterable(endpoints.map { endpoint -> lazy { factory(endpoint) } })

    override fun log(indexPattern: String, date: String, params: Map<String, String>): Call<ResponseBody> {
        return roundRobin.next().value.log(
            indexPattern = indexPattern,
            date = date,
            params = params
        )
    }
}
