package com.avito.emcee.worker.internal.rest.handler

import com.avito.emcee.worker.internal.rest.HttpMethod

// TODO: store currently processing buckets
internal class ProcessingBucketsRequestHandler : RequestHandler(HttpMethod.POST, "/currentlyProcessingBuckets", {
    """
        |{ 
        |    "bucketIds":[] 
        |}
    """.trimMargin()
})
