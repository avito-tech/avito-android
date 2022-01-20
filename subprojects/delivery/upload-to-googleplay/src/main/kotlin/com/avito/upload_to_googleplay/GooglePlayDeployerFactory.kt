package com.avito.upload_to_googleplay

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import org.slf4j.Logger
import java.io.File
import java.util.concurrent.TimeUnit

public interface GooglePlayDeployerFactory {

    public fun create(logger: Logger): GooglePlayDeployer
}

public class RealGooglePlayDeployerFactory(private val jsonKey: File) : GooglePlayDeployerFactory {

    private val timeout = TimeUnit.MINUTES.toMillis(5).toInt()

    override fun create(logger: Logger): GooglePlayDeployer {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val factory = JacksonFactory.getDefaultInstance()
        val credential: GoogleCredential = GoogleCredential.fromStream(
            jsonKey.inputStream(),
            transport,
            factory
        ).createScoped(listOf(AndroidPublisherScopes.ANDROIDPUBLISHER))

        val androidPublisher = AndroidPublisher.Builder(
            transport,
            factory,
        ) { request ->
            credential.initialize(
                request
                    .setConnectTimeout(timeout)
                    .setReadTimeout(timeout)
            )
        }.run {
            applicationName = "avito-google-play-publisher"
            build()
        }

        return GooglePlayDeployerImpl(
            publisher = androidPublisher,
            logger = logger
        )
    }
}

public class MockGooglePlayDeployerFactory(private val mockWebServerUrl: String) : GooglePlayDeployerFactory {

    override fun create(logger: Logger): GooglePlayDeployer {
        val transport = NetHttpTransport.Builder().build()
        val credential = MockGoogleCredential.Builder().build()
        val factory = JacksonFactory.getDefaultInstance()

        val androidPublisher = AndroidPublisher.Builder(
            transport,
            factory,
        ) { request -> credential.initialize(request) }.run {
            applicationName = "avito-google-play-publisher"
            rootUrl = mockWebServerUrl
            build()
        }

        return GooglePlayDeployerImpl(
            publisher = androidPublisher,
            logger = logger
        )
    }
}

public object GooglePlayDeployerFactoryProducer {

    public fun create(jsonKey: File, mockWebServerUrl: String?): GooglePlayDeployerFactory {
        return if (mockWebServerUrl.isNullOrBlank()) {
            RealGooglePlayDeployerFactory(jsonKey)
        } else {
            MockGooglePlayDeployerFactory(mockWebServerUrl)
        }
    }
}
