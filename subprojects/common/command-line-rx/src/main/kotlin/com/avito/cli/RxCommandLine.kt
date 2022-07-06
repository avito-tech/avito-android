package com.avito.cli

import com.avito.cli.Notification.Exit
import com.avito.cli.Notification.Output
import rx.Emitter
import rx.Observable
import java.io.File

public class RxCommandLine(command: String, args: List<String>) : CommandLine(command, args) {

    public fun start(output: File? = null): Observable<Notification> =
        Observable.create(
            { emitter ->
                emitter.setCancellation { close() }
                startInternal(
                    output,
                    { notification ->
                        when (notification) {
                            is Output -> emitter.onNext(notification)
                            is Exit -> {
                                emitter.onNext(notification)
                                emitter.onCompleted()
                            }
                        }
                    },
                    { error ->
                        emitter.onError(error)
                    }
                )
            },
            Emitter.BackpressureMode.ERROR
        )
}
