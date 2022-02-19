Docker image to build other images.

## How to work with this project

Open this directory as a project in IDE.

Build with tests:

```shell
./gradlew build
```

Run a command:

```shell
./gradlew run --args="build ..."
```

Build image locally:

```shell
../build.sh image-builder
```

This and nearby scripts uses previously published version of image-builder itself.
This approach sometimes called a bootstrapping.
