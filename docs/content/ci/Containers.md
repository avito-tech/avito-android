# Сборки в контейнерах

--8<--
avito-disclaimer.md
--8<--

[Avito Docker documentation (internal)](http://links.k.avito.ru/cfxOMToAQ)

Все образы расположены в `ci/docker`.

## Android builder image

This is the image for building and testing Android applications. It contains Android SDK.

### How to update android-builder image?

1. Build the image to test your changes

=== "In CI"

    Run [Build android-builder (internal)](http://links.k.avito.ru/tmctAvitoAndroidBuilder) teamcity configuration.  
    You will see the tag in stdout:
    
    ```text
    Published the image <docker registry>/android/builder:<tag>
    ```

=== "Locally"

    If you need to test locally before publishing:

    ```bash
    # Docker registry to further publishing
    export DOCKER_REGISTRY=...
    # DockerHub credentials
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
    # DockerHub credentials
    export DOCKER_HUB_USERNAME=...
    export DOCKER_HUB_PASSWORD=...
    cd ci/docker
    ./publish.sh <directory with Dockerfile>
    ```
    
    You will see in stdout:
    
    ```text
    Published the image <docker registry>/android/builder:<tag>
    ```

1. [Upload the image to Docker Hub](#uploading-image-to-docker-hub)
1. Update image hash in `IMAGE_ANDROID_BUILDER` variable in ci shell scripts:
    - In GitHub repo: `ci/_environment.sh` 
    - In internal avito repository: `ci/_main.sh`
1. Check this images is working. At least, run `ci/local_check.sh`.
1. Make PR with a new image.

## Image builder

Образ для сборки других docker образов.

### Как обновить

Образ собирает сам себя с помощью предыдущей версии образа (bootstrapping):  
`./publish.sh image-builder`  
`publish.sh` - использует текущую версию образа.  
Директория `image-builder` - содержит изменения.

Если меняем контракт с окружением, то вносим правки поэтапно, чтобы прошлая версия образа могла собрать новую.

Teamcity configuration: [Build image-builder (internal)](http://links.k.avito.ru/Bt2)

## Android emulator images

Эмуляторы имеют кастомные настройки, оптимизированы для стабильности и производительности.

- Небольшое разрешение экрана: 320x480, 4 inch
- Отключены многие фичи

### Как запустить эмулятор?

=== "macOS/Windows"

    CI эмулятор невозможно запустить из-за ограничений виртуализации [haxm #51](https://github.com/intel/haxm/issues/51#issuecomment-389731675).
    Поэтому воспроизводим идентичную конфигурацию.
    
    - Создай эмулятор в Android Studio: WVGA (Nexus One) с размером экрана 3.4'' и разрешением 480x800.
    - Запусти эмулятор
    - Настрой параметры:
    
    ```bash
    adb root
    adb shell "settings put global window_animation_scale 0.0"
    adb shell "settings put global transition_animation_scale 0.0"
    adb shell "settings put global animator_duration_scale 0.0"
    adb shell "settings put secure spell_checker_enabled 0"
    adb shell "settings put secure show_ime_with_hard_keyboard 1"
    adb shell "settings put system screen_off_timeout 1800000"
    adb shell "settings put secure long_press_timeout 1500"
    ```
    
    - Перезагрузи эмулятор
    
    См. все настройки в `android-emulator/hardware` и `android-emulator/prepare_snapshot.sh`
    
    [Задача на автоматизацию (internal)](http://links.k.avito.ru/MBS7122)

=== "Linux"

    Проще и надежнее использовать оригинальные CI эмуляторы.
    
    Требования:
    
    - Docker
    - [KVM](https://developer.android.com/studio/run/emulator-acceleration#vm-linux)
    
    
    - Найди актуальную версию образа в `Emulator.kt`.
    - Разреши подключение к Xorg серверу с любого хоста (изнутри контейнера в нашем случае):
    
    ```bash
    xhost +
    ```
    
    - Запусти эмулятор:
    
    ```bash
    docker run -d \
        -p 5555:5555 \
        -p 5554:5554 \
        -e "SNAPSHOT_ENABLED"="false" -e "WINDOW"="true" --volume="/tmp/.X11-unix:/tmp/.X11-unix:rw" \
        --privileged \
        <registry>/android/emulator-27:<TAG>
    ```
    
    Или в headless режиме:
    
    ```bash
    docker run -d \
        -p 5555:5555 \
        -p 5554:5554 \
        --privileged \
        <registry>/android/emulator-27:<TAG>
    ```
    
    - Подключись к эмулятору в adb
    
    ```bash
    adb connect localhost:5555
    ```

### Как обновить образ?

Для эмулятора нужна более сложная подготовка, поэтому используем отдельные скрипты и образы.

#### 1. Залей образы в приватный Docker registry

=== "CI"

    1. Собери образ на ветке в Teamcity конфигурации [Build android-emulator (internal)](http://links.k.avito.ru/Y3).  
    Теги новых образов будут в артефактах сборки.
    1. Обнови теги в build.gradle скриптах.
    1. Запушь изменение в ветку.

=== "Local"

    Требования:
    
    - Linux, docker
    - [KVM](https://developer.android.com/studio/run/emulator-acceleration#vm-linux)
    - K8S права на push образов в registry-mobile-apps (env переменные DOCKER_LOGIN, DOCKER_PASSWORD)
    
    1. Запусти скрипт:
    
    ```bash
    cd ci/docker
    ./publish_emulator.sh android-emulator 30
    ``` 
    
    Соберет образ, протестирует и запушит в docker registry.
    
    1. Найти новые теги образов.
    См. stdout: "Published the image ..."
    1. Обнови теги образов в скриптах.

#### 2. Залей образы в Docker hub

[Uploading image to Docker Hub](#uploading-image-to-docker-hub)

### Как проверить регрессию?

- Прогони instrumentation dynamic, чтобы выявить возможную утечку памяти.  
Для этого запусти компонентный тест с большим числом повторов.
- Прогони prCheck.  
Сравни количество тестов по всем статусам, не стало ли больше упавших или потерянных.

### Как проверить сколько ресурсов тратит эмулятор?

Локально используем [cAdvisor](https://github.com/google/cadvisor)

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

В CI смотрим в метрики куба.

## Docker Hub

Образы публикуем в [hub.docker.com/u/avitotech](https://hub.docker.com/u/avitotech).

### Uploading image to Docker Hub

Пока что заливаем вручную, задача на автоматизацию: MBS-8773.

1. Залогинься в Docker hub:
    ```bash
    docker login --username=avitotech --password=...
    ```
1. Скачай новый образ из приватного registry:
    ```bash
    docker pull <DOCKER_REGISTRY>/<repository>/<image>:<TAG>
    ```
    Пример:
    ```bash
    docker pull inhouse-registry/android/android-emulator-29:c0de63a4cd
    ```
1. Поставь образу такой-же тег, но имя для репозитория в DockerHub:
    ```bash
    docker tag <SOURCE IMAGE> avitotech/android-emulator-<API>:<TAG>
    ```
    Пример:
    ```bash
    docker tag inhouse-registry/repository/android-emulator-29:c0de63a4cd avitotech/android-emulator-29:c0de63a4cd`
    ```
    
    ???+ info
        Первоначальный уникальный tag получаем из digest. 
        Проставляем его как tag, потому-что [digest в разных registry может не совпадать](https://github.com/docker/distribution/issues/1662#issuecomment-213079540).

1. Залей образ:
    ```bash
    docker push <IMAGE>:<TAG>
    ```
    Пример:
    ```bash
    docker push avitotech/android-emulator-29:c0de63a4cd
    ```

## Best practices

### Reproducible image

Хотим получать одинаковый образ на любой машине, в любом окружении. 
Это упрощает отладку проблем и делает сборку более надежной. 

[reproducible-builds.org](https://reproducible-builds.org/docs/definition/)

Источники нестабильности:

- Не указана явно версия зависимости.
- Копируем в образ файлы, сгенерированные вне докера.   
Глядя на такие файлы трудно сказать в каком окружении они созданы, какое содержание ожидаемое.
