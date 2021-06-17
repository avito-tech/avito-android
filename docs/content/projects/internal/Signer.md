# Signer gradle plugin

Sign APK's and AAB's(bundles) using in-house service.

## Usage

### Apply plugin to application module

```kotlin
plugins {
    id("com.android.application")
    id("com.avito.android.signer")
}
```

### Specify service host

```kotlin
signService {
    host.set("https://my-inhouse-signer-service.service/path")
}
```

### Register which buildVariants to sign

```kotlin
signService {
    apk(
        variant = android.buildTypes.release,
        token = project.properties.get("avitoSignToken")
    )
    bundle(
        variant = android.buildTypes.release,
        token = project.properties.get("avitoSignBundleToken")
    )
}
```

Variants not listed here will fallback
to [default Android Gradle Plugin signing mechanism](https://developer.android.com/studio/publish/app-signing)

### Use enabled to avoid signing

```kotlin
signService {
    enabled.set(false)
}
```

Can be useful to disable signing in different builds by some condition

### Plugin will generate tasks

- `signApkViaService<Variant>`
- `signBundleViaService<Variant>`

Tasks will not be wired by the plugin to assemble. User should do it manually, configuring corresponding dependencies,
e.g. `dependsOn(signApkViaService<Variant>)`

## Local development behavior

By default local development not impacted at all:  
Typical `assemble`, `install` tasks won't add signer tasks as dependencies.

Signer tasks can be called locally, just don't forget to specify token as gradle property, like `-PavitoSignToken=XXX`

## Relations with CiSteps plugin

Signer tasks dependency implicitly wired
in [Artifacts collection steps of CiSteps plugin](../CiSteps/#collecting-artifacts)

See: `com.avito.ci.steps.VerifyArtifactsStep`
