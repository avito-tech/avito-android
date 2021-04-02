# Networking

We use [Okhttp](https://square.github.io/okhttp/) sometimes with [Retrofit](https://square.github.io/retrofit/) for
networking in Gradle plugins and Android libraries.

## Obtaining an OkHttpClient

Add dependency on `http-client` module:

```kotlin
dependencies {
    implementation(projects.common.httpClient)
}
```

Create an instance of `HttpClientProvider`

```kotlin
val httpClientProvider = HttpClientProvider(statsdSender, timeProvider)
```

Get the client:

```kotlin
val httpClient = httpClientProvider.provide().build()
```

Method provide() returns `OkHttpClient.Builder` so you can configure it further

## Required tag

Obtained client needs some additional information to be able to gather required statistics.

Every request should contain a tag of type `RequestMetadata`

???+ warning 
    Missing tag will lead to missing metrics for this api method / service 
    Warning message could be found in logs

### Creating OkHttpRequest manually

```kotlin
Request.Builder()
    .url("some url")
    .tag(RequestMetadata::class.java, RequestMetadata("some-service", "some-method"))
    .build()
```

### Using Retrofit

```kotlin
interface SomeApi {

    @POST("/")
    fun someMethod(
        @Body someBody: String,
        @Tag metadata: RequestMetadata = RequestMetadata("some-service", "some-method")
    ): Call<Unit>
}
```
