package com.avito.alertino

import com.avito.logger.LoggerFactory

public object AlertinoSenderFactory {

    public fun create(baseUrl: String, loggerFactory: LoggerFactory): AlertinoSender {
        return AlertinoClient(baseUrl, loggerFactory)
    }
}
