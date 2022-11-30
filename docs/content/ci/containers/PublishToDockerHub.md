# How to publish images to Docker Hub

Previously we publish images to [hub.docker.com/u/avitotech](https://hub.docker.com/u/avitotech). \  
Now publication is at pause.

Automatization task MBS-8773

1. Login to Docker hub:
    ```bash
    docker login --username=avitotech --password=...
    ```
2. Download new image from private registry:
    ```bash
    docker pull <DOCKER_REGISTRY>/<repository>/<image>:<TAG>
    ```
   Example:
    ```bash
    docker pull inhouse-registry/android/android-emulator-29:c0de63a4cd
    ```
3. Set image the same tag but name for repository DockerHub:
    ```bash
    docker tag <SOURCE IMAGE> avitotech/android-emulator-<API>:<TAG>
    ```
   Example:
    ```bash
    docker tag inhouse-registry/repository/android-emulator-29:c0de63a4cd avitotech/android-emulator-29:c0de63a4cd`
    ```

   ???+ info
   Unique tag gather from digest.
   Set it as tag, because [digest in different registry could be different](https://github.com/docker/distribution/issues/1662#issuecomment-213079540).

4. Deploy the image to DockerHub:
    ```bash
    docker push <IMAGE>:<TAG>
    ```
   Example:
    ```bash
    docker push avitotech/android-emulator-29:c0de63a4cd
    ```
