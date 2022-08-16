package com.avito.emcee.server.fake

import com.avito.emcee.queue.JobResultsResponse
import com.avito.emcee.queue.JobStateResponse
import com.avito.emcee.queue.QueueState
import com.avito.emcee.queue.ScheduleTestsResponse
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

public fun main() {
    embeddedServer(Netty, port = 41000) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            get("/") {
                call.respond("Hello from the queue")
            }
            post("/scheduleTests") {
                call.respond(ScheduleTestsResponse("stub"))
            }
            post("/jobResults") {
                call.respond(JobResultsResponse(JobResultsResponse.JobResults("stub", emptyList())))
            }
            post("/jobState") {
                call.respond(
                    JobStateResponse(
                        JobStateResponse.JobState(
                            "stub",
                            QueueState(
                                "stub",
                                QueueState.RunningQueueState(
                                    dequeuedBucketCount = 0,
                                    dequeuedTests = emptyList(),
                                    enqueuedBucketCount = 0,
                                    enqueuedTests = emptyList()
                                )
                            )
                        )
                    )
                )
            }
        }
    }.start(wait = true)
}
