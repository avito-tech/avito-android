# Networking

We use [Okhttp](https://square.github.io/okhttp/) and [Retrofit](https://square.github.io/retrofit/) for
networking in Gradle plugins and Android libraries.

## How to observe your plugin http request stability

Add `StatsHttpEventListener` from module `subprojects:common:http-statsd` to your `OkHttpClient.Builder`

1. Add module dependency

    ```kotlin
    dependencies {
        implementation(projects.subprojects.common.httpStatsd)
    }
    ```

2. Add `StatsHttpEventListener`

    ```kotlin
    OkHttpClient.Builder()
        .eventListenerFactory {
            StatsHttpEventListener(
                statsDSender = statsdSender,
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                requestMetadataProvder = requestMetadataProvder,
            )
        }
    ```

3. Add RequestMetadata to your request `tag` corresponding to your requestMetadataProvider type

    ???+ warning
        Missing tag will lead to missing metrics for this api method / service
        Warning message could be found in logs


    ```kotlin
    Request.Builder()
        .url("some url")
        .tag(RequestMetadata::class.java, RequestMetadata("some-service", "some-method"))
        .build()
    ```
    
    or
    
    ```kotlin
    interface SomeApi {
    
        @POST("/")
        fun someMethod(
            @Body someBody: String,
            @Tag metadata: RequestMetadata = RequestMetadata("some-service", "some-method")
        ): Call<Unit>
    }
    ```
