---
title: Сборки в контейнерах
type: docs
---

# Сборки в контейнерах

{{<avito page>}}

[Avito Docker documentation (internal)](http://links.k.avito.ru/cfxOMToAQ)

Все образы расположены в `ci/docker`.

## Android SDK

Базовый образ c build tools. (будет сделан в MBS-7071)

## Android builder

Образ который умеет собирать приложение в CI.

### How to update?

- Собери образ локально для проверки изменений:

```bash
cd ci/docker
./publish <папка с dockerfile>`
```

В output будет новый tag образа. 
- Обнови тег образа в `_main.sh` в переменной `IMAGE_ANDROID_BUILDER`
- Убедись что образ отрабатывает корректно при локальном использовании.  
Прогони хотя-бы `ci/local_check.sh`. 

- Собери образ на ветке в teamcity конфигурации [Build android-builder (internal)](http://links.k.avito.ru/tmctAvitoAndroidBuilder)  
В этом проекте зашита авторизация для доступа к registry.
- Обнови тег в `_main.sh`
- Запушь изменение в ветку.

## Docker in docker

Утилитарный образ с докером внутри.\ 
Используем внутри скриптов для создания и публикации других образов, прежде всего эмулятора.

### How to update itself?

Образ собирает сам себя с помощью предыдущей версии образа (bootstrapping):\
`./publish.sh docker-in-docker-image`\
`publish.sh` - использует текущую версию образа\
`docker-in-docker-image` - содержит изменения

Если меняем контракт с окружением, то вносим правки поэтапно, чтобы прошлая версия образа могла собрать новую.

[Build docker-in-docker (internal)](http://links.k.avito.ru/tmctAvitoAndroidDockerInDocker)

## Android emulator

Эмуляторы имеют кастомные настройки, оптимизированы для стабильности и производительности.

- Небольшое разрешение экрана: 320x480, 4 inch
- Отключены многие фичи

### Как запустить эмулятор?

{{< tabs "run emulator" >}}
{{< tab "OSX/Windows" >}}

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
{{< /tab >}}
{{< tab "Linux" >}} 

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
    -e "SNAPSHOT_DISABLED"="true" -e "WINDOW"="true" --volume="/tmp/.X11-unix:/tmp/.X11-unix:rw" \
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

{{< /tab >}}
{{< /tabs >}}

### Как обновить образ?

Для эмулятора нужна более сложная подготовка, поэтому используем отдельные скрипты и образы.

{{< tabs "update emulator" >}}
{{< tab "CI" >}}

1. Собери образ на ветке в teamcity конфигурации [Build android-emulator (internal)](http://links.k.avito.ru/tmctAvitoAndroidEmulatorImageBuilder).  
Теги новых образов будут в файле в артефактах сборки.
1. Обнови теги в Devices.kt
1. Запушь изменение в ветку.

{{< /tab >}}
{{< tab "Local" >}}

Требования:

- Linux, docker
- [KVM](https://developer.android.com/studio/run/emulator-acceleration#vm-linux)
- K8S права на push образов в registry-mobile-apps (env переменные DOCKER_LOGIN, DOCKER_PASSWORD)

1. Запусти скрипт:

```bash
cd ci/docker
./publish_emulator android-emulator
``` 

Соберет образ, протестирует и запушит в docker registry.

1. Найти новые теги образов.
См. stdout скрипта или файл `android-emulator/images.txt`
1. Обнови теги образов в `_main.sh`

{{< /tab >}}
{{< /tabs >}}

### Как проверить регрессию?

- Прогони instrumentation dynamic чтобы выявить возможную утечку памяти.\
Для этого запусти компонентный тест с большим числом повторов.
- Прогони fullCheck\
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

## Best practices

### Reproducible image

Хотим получать одинаковый образ на любой машине, в любом окружении. 
Это упрощает отладку проблем и делает сборку более надежной. 

[reproducible-builds.org](https://reproducible-builds.org/docs/definition/)

Источники нестабильности:

- Не указана явно версия зависимости.
- Копируем в образ файлы, сгенерированные вне докера.   
Глядя на такие файлы трудно сказать в каком окружении они созданы, какое содержание ожидаемое.
