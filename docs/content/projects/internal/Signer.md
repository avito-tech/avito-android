# Signer gradle plugin

Sign APK's and AAB's(bundles) using in-house service.

## Usage

### Apply plugin to application module

```kotlin
plugins {
    id("com.android.application")
    id("com.avito.android.sign-service")
}
```

### Specify service host

```kotlin
signer {
    serviceUrl.set("https://my-inhouse-signer-service.service/path")
    readWriteTimeoutSec.set(90)
}
```

### Register which packages to sign

```kotlin
signer {
    apkSignTokens.put(
        "com.avito.android",
        project.properties.get("appApkToken")
    )
    bundleSignTokens.put(
        "com.avito.android",
        project.properties.get("appBundleToken")
    )
}
```

Variants not listed here will fallback
to [default Android Gradle Plugin signing mechanism](https://developer.android.com/studio/publish/app-signing)

### Disable to avoid signing

```kotlin
signService {
    enabled.set(false)
}
```

Can be useful to disable signing in different builds by some condition

### Plugin will generate tasks

- `signApkViaService<Variant>`
- `signBundleViaService<Variant>`

## Local development behavior

By default local development not impacted at all:  
Typical `assemble`, `install` tasks won't add signer tasks as dependencies.

Signer tasks can be called locally, just don't forget to specify tokens

## Metrics ([Internal Grafana](http://links.k.avito.ru/AndroidExternalServicesGrafana))

Signer service http calls data available under `<namespace>.signer.sign.*`.

## Troubleshooting

Network call data posted right into exception message that failing the build.  
It should be sent to service owners for investigation

Example: 

```text
Can't sign: /tmp/junit7019843706336345100/app/build/outputs/signService/apk/release/app-release.apk
Where : Signing artifact via service
You can learn more about this problem at https://avito-tech.github.io/avito-android/projects/internal/Signer/#troubleshooting
Cause exception message: Failed to sign /tmp/junit7019843706336345100/app/build/outputs/apk/release/app-release-unsigned.apk via service
Request: POST http://localhost:54801/sign
Request body size: 539997 bytes
Response: 404
Response headers:
Content-Length: 0
Response body is empty
```
