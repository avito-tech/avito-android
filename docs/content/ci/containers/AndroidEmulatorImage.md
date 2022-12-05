Emulators are build with custom optimized settings for stability and performance. \ 
For example a low screen resolution 320x480, 4 inch.
All settings you can find in `./ci/android-emulator/hardware/config_*.ini` files

## How to add an emulator that supports new SDK version 

All supported emulators configs you could find at `./ci/android-emulator/hardware/config_*.ini` \
If there are no needed config you should:

1. Copy-paste closest `config_*.ini`
2. Change `image.sysdir.1` property to your sdk path i.e. `system-images/android-22/google_apis/x86/` for SDK 22 
3. Upload emulator system image to [artifactory](http://links.k.avito.ru/emulator-system_images)
   1. You could find a link to system image at Android Studio SDK Manager. Try to download image you will see the link.
   2. Unzip image and change internal structure of folders to `system-image/android-<sdk>/google_apis/<image-arch>`. You could example at any image in [artifactory](http://links.k.avito.ru/emulator-system_images)
4. [Publish an emulator](#how-to-publish-a-new-emulator-image)

???+ info
    We change internal structure of folders inside emulator system image zip archive because we want to add ability for users to extract and use images locally

## How to build an emulator image

To build an android emulator image you need:
- Linux, docker
- [KVM](https://developer.android.com/studio/run/emulator-acceleration#vm-linux)

Better to build image at CI because image will guarantee more hermetic. And build locally only for testing

To build:

### CI
Run [Teamcity configuration with needed API level](http://links.k.avito.ru/Sc)

### Local
1. Run script

```bash
cd ci/docker
./build_emulator.sh android-emulator <api version>
```

2. Find image tag at logs. You can use it for testing image locally

## How to test a new emulator image

???+ warn
    Ensure that app supports SDK level of your emulator. Simply run test against built from Android Studio emulator with same SDK level.

1. Run [instrumentation dynamic](http://links.k.avito.ru/nl) for one test on one already supported emulators and your one
   1. Compare the test status, time execution, steps
2. Run [instrumentation dynamic](http://links.k.avito.ru/nl) for any component test with many executions i.e. 100
   1. It will help check memory leaks. If there are no strange errors everything is ok
3. Run [prCheck](https://tmct.avito.ru/buildConfiguration/AvitoAndroid_Build)
   1. Compare amount of tests at all statuses. There should be no LOST,FAILED,ERROR tests

## How to check amount of resources consumed by an emulator image

### CI
Check [dashboards](https://mntr.avito.ru/grafana/d/9LxwD7Wnz/android-emulators)

### Local
Use [cAdvisor](https://github.com/google/cadvisor)

    ```bash
    sudo docker run \
      --volume=/:/rootfs:ro \
      --volume=/var/run:/var/run:ro \
      --volume=/sys:/sys:ro \
      --volume=/var/lib/docker/:/var/lib/docker:ro \
      --volume=/dev/disk/:/dev/disk:ro \
      --publish=8080:8080 \
      --detach=true \
      --name=cadvisor \
      google/cadvisor:latest
    ```

## How to publish a new emulator image

1. Run at Teamcity [Build and publish android-emulator (internal)](http://links.k.avito.ru/publish-android-emulator-image)

???+ info
    You will need `Image tag` and `Avd model` when try to add new emulator to Test Runner. You could copy-paste them from `Teamcity Build Log`. Image tag from the end of the Build Log. Avd model - Search phrase `Print devices`. We print `adb devices -l` from `prepare_emulator.sh`   


## How to run an emulator image

### MacOS and windows

MacOS and Windows are unsupported because of virtualization [haxm #51](https://github.com/intel/haxm/issues/51#issuecomment-389731675)

But you could [create emulator at Android Studio and configure it](http://links.k.avito.ru/emulator-setup)

### Linux

It's easier and reliable to use original CI emulator images

Requirements:

- Docker
- [KVM](https://developer.android.com/studio/run/emulator-acceleration#vm-linux)


- Find actual emulator image `Emulator.kt`.
- Add permission for connection to Xorg from any host. In our case from emulator image container
```bash
xhost +
```
- Run emulator:
```bash
docker run -d \
    -p 5555:5555 \
    -p 5554:5554 \
    -e "SNAPSHOT_ENABLED"="false" -e "WINDOW"="true" --volume="/tmp/.X11-unix:/tmp/.X11-unix:rw" \
    --privileged \
    <registry>/android/emulator-27:<TAG>
```
Or run with headless mode:
```bash
docker run -d \
    -p 5555:5555 \
    -p 5554:5554 \
    --privileged \
    <registry>/android/emulator-27:<TAG>
```
- Connect to emulator via adb
```bash
adb connect localhost:5555
```
