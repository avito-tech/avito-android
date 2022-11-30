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
