# How to increase compileSdk

## Step 1: Prepare `android-builder` image

###[How to prepare android-builder for a new compileSdk](./containers/AndroidBuilderImage.md#how-to-prepare-android-builder-for-a-new-compilesdk)

## Step 2: Bump `android-buidler` version

Update builder image version in `ci/_environment.sh` and `Makefile`

```bash
# ci/_environment.sh
IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder-hermetic:<YOUR_IMAGE_TAG>
```

```makefile
# Makefile
ANDROID_BUILDER_TAG=<YOUR_IMAGE_TAG>
ifeq ($(origin DOCKER_REGISTRY),undefined)
    IMAGE_ANDROID_BUILDER=avitotech/android-builder:$(ANDROID_BUILDER_TAG)
else
    IMAGE_ANDROID_BUILDER=$(DOCKER_REGISTRY)/android/builder-hermetic:$(ANDROID_BUILDER_TAG)
endif
```

## Step 3: Update `libs.versions.toml`

Update compileSdk and buildTools (if you updated the version in android-builder)
```properties
buildTools = "36.0.0"
compileSdk = "36"
```

## Step 4: Fix possible compilation errors

## Step 5: Update the `maximumSupportedSdk`

Set the maximum supported sdk in `com.avito.android.util.DeviceSettingsChecker`:

```kotlin
public class DeviceSettingsChecker(
    // ...
    private val maximumSupportedSdk: Int = Build.VERSION_CODES.BAKLAVA,
    // ...
)
```

## Step 6: Release new infra version

###[How to make a new infra release](/contributing/Release)
