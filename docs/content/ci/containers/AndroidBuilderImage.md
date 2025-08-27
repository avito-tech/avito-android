This is the image for building and testing Android applications. It contains Android SDK.

## How to update android-builder image

1. Build the image to test your changes

=== "In CI"

    Run [Build android-builder (internal)](http://links.k.avito.ru/FO) teamcity configuration.  
    You will see the tag in stdout:
    
    ```text
    Published the image <docker registry>/android/builder:<tag>
    ```

=== "Locally"

    If you need to test locally before publishing:

    ```bash
    # Docker registry to further publishing
    export DOCKER_REGISTRY=...
    # DockerHub credentials (optional)
    export DOCKER_HUB_USERNAME=...
    export DOCKER_HUB_PASSWORD=...
    cd ci/docker
    ./build.sh <directory with Dockerfile>
    ```

    You will see in stdout:
    ```
    Image <image id> tagged as <docker registry>/android/image-builder:<tag>
    ```

    Continue to further publishing:

    ```bash
    # Docker registry to publish
    export DOCKER_REGISTRY=...
    export DOCKER_REGISTRY_USERNAME=...
    export DOCKER_REGISTRY_PASSWORD=...
    # DockerHub credentials (optional)
    export DOCKER_HUB_USERNAME=...
    export DOCKER_HUB_PASSWORD=...
    cd ci/docker
    ./publish.sh <directory with Dockerfile>
    ```
    
    You will see in stdout:
    
    ```text
    Published the image <docker registry>/android/builder:<tag>
    ```
1. Update image hash in `IMAGE_ANDROID_BUILDER` variable in ci shell scripts:
    - In GitHub repo: `ci/_environment.sh`
    - In internal avito repository: `ci/_main.sh`
1. Check this images is working. At least, run `ci/local_check.sh`.
1. Make PR with a new image.

## How to prepare ndk archive

via TeamCity job: [Prepare ndk archive (internal)](http://links.k.avito.ru/prepare-ndk)

OR Manually:

1. Download [ndk](https://developer.android.com/ndk/downloads) for linux
1. If you are using MacOS make sure you are working in volume with [case-sensitive file system](https://support.apple.com/en-am/guide/disk-utility/dsku19ed921c/mac). Create new volume using "Disk Utility" and copy archive there if you don't have case-sensitive volume.
1. Unpack zip archive using `unzip <zip file>` command in terminal to verify that archive is fully unpacked. Do not use double-click. If you see replace prompt, check previous step.
1. Change file structure to `ndk/<version>/<ndk files>`
1. Create zip archive using `zip` tool with `-ry9` flags

For example for `25.2.9519653` do:
   ```shell
   unzip android-ndk-r25c-linux.zip;
   mv android-ndk-r25c 25.2.9519653;
   mkdir ndk;
   mv 25.2.9519653 ndk/25.2.9519653;
   zip -ry9 ndk-linux-25_2_9519653.zip ndk;
   ```
<br><br>

# How to prepare `android-builder` for a new `compileSdk`

The process of updating the android-builder image consists of three main steps:

- preparing dependencies
- building the image
- and updating the image in the project.

### Image types

- **Hermetic** — uses pre-packaged archives from Artifactory
- **Non-hermetic** — downloads SDK components directly from Google repositories

## Step 1: Preparing Android SDK dependencies

Before updating the image, upload new versions of Android SDK components to Artifactory.

Required components:

- platforms (e.g., 36.0.2 for API 36)
- build-tools (may be required to update AGP)

???+ info "Optional components" 
    platform-tools, cmdline-tools, cmake, and ndk are usually not required to update compileSdk. <br>
    You may update them to current versions if desired, or if it's eventually required for newer compileSdk. <br>
    Also, make sure that the new versions don’t slow down builds.

### Automated method (recommended)

Use the ready-made scripts in [TeamCity (internal)](http://links.k.avito.ru/upload-sdk-files-tc) <br>
Or invoke them manually from the [repository (internal)](http://links.k.avito.ru/upload-sdk-files-script-repo)

### Manual method

If automated scripts are unavailable:

1. Check available packages via Android SDK Manager:
```bash
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list
```

1. Download required components via Android SDK Manager:
```bash
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install 'platforms;android-36'
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install 'build-tools;36.0.0'
```

1. Create archives with the correct structure: <br>
See examples of ready archives in [Artifactory](http://links.k.avito.ru/archives-examples). <br>
Pack the downloaded components with the same archive structure.
```bash
cd $ANDROID_HOME
zip -r platforms_36_0_2.zip platforms/android-36/
```

1. Upload the archives to Artifactory.

## Step 2: Updating the android-builder image

### Hermetic image

Edit `./ci/docker/android-builder/hermetic/Dockerfile`:

```dockerfile
# Update archive versions
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platforms/platforms_36_0_2.zip $ANDROID_HOME
```

### Non-hermetic image

Update `./ci/docker/android-builder/non-hermetic/packages.txt`:

```
build-tools;36.0.0
platforms;android-36
tools
extras;google;google_play_services
extras;google;m2repository
ndk;25.2.9519653
cmake;3.22.1
```

## Step 3: Build and update image in the project

### [Build and publish the image](#how-to-update-android-builder-image)

