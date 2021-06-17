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

### Plugin will generate tasks

- `signApkViaService<Variant>`
- `signBundleViaService<Variant>`

Tasks will not be wired by the plugin to assemble. User should do it manually, configuring corresponding dependencies,
e.g. `dependsOn(signApkViaService<Variant>)`

## Local development behavior

If tokens not set, task will be skipped and default signing mechanism will be used
