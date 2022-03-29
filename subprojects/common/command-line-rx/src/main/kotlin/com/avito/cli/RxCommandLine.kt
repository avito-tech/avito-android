package com.avito.cli

import com.avito.cli.CommandLine.Notification.Internal.Error
import com.avito.cli.CommandLine.Notification.Public.Exit
import com.avito.cli.CommandLine.Notification.Public.Output
import rx.Emitter
import rx.Observable
import java.io.File

public fun CommandLine.executeAsObservable(
    output: File? = null
): Observable<CommandLine.Notification.Public> = Observable.create(
    { emitter ->
        emitter.setCancellation { close() }
        start(output) { notification ->
            when (notification) {
                is Output -> emitter.onNext(notification)
                is Error -> emitter.onError(notification.error)
                is Exit -> {
                    emitter.onNext(notification)
                    emitter.onCompleted()
                }
            }
        }
    },
    Emitter.BackpressureMode.ERROR
)
