---
title: Infrastructure publication
type: docs
---

# Avito Android Infrastructure

Open source инфраструктурная часть android проектов Avito.

## Структура проекта

`:android` 

Набор расширений над android gradle plugin и над android sdk (TODO разделить на два модуля)

`:bitbucket`

Клиент для публикации деталей проверок на CI в открытый PullRequest на Bitbucket Server.

`:build-checks`

Gradle plugin для раннего обнаружения проблем в параметрах сборки

`:build-metrics`

Gradle plugin для сбора и отправки метрик сборки (стэк: statsd, graphite, grafana)

`:build-properties`

Gradle plugin для доставки параметров окружения в ассеты сборки android проекта

`/ci/docker/android-builder`

Docker образ для сборки самого проекта Avito Android Infrastructure

`:docker`

Клиент для работы с docker

`:enforce-repos`

Gradle plugin для настройки используемых репозиториев зависимостей

`:file-storage`

Клиент для in-house сервиса хранения бинарных файлов (пример: для скриншотов и видео в отчетах тестов)

`:git`

Обертка для работы с git в gradle

`:impact`, `:impact-plugin`

Gradle Plugin для поиска задетых изменениями модулей относительно target ветки\
[см. Импакт Анализ]({{< ref "/ci/ImpactAnalysis.md" >}})

`:instrumentation-impact-analysis`, `:ui-test-bytecode-analyser`

Gradle plugin и вспомогательный модуль для поиска связи изменений найденных при помощи `impact-plugin` и UI тестов 

`:kotlin-config`

Gradle plugin для настройки всех kotlin модулей в проектах android-приложениях

`:kotlin-dsl-support`

Расширения, в основном скопированные из kotlin-dsl (но также несколько своих).\
Нужны для сохранения API в gradle скриптах, т.к. из-за нестабильности kotlin-dsl мы то пользуемся, то отказываемся от него

`:lint-report`

Gradle plugin, который склеивает lint репорты из разных модулей для отображения на одной странице

`:logging`

Кастомный логгер для использования внутри gradle worker'ов, т.к. gradle logger не сериализуется и имеет ссылку на project(todo: уже не актуально)

`:okhttp`

Расширения над okhttp, которыми активно пользуемся как в плагинах так и в android приложении

`:robolectric-config`

Gradle plugin для настройки всех модулей в проектах android-приложениях, которые используют robolectric

`:room-config`

Gradle plugin для настройки всех модулей в проектах android-приложениях, которые используют room

`:runner:client`, `:runner:service`, `:runner:shared`, `:runner:shared-test`

Кастомный раннер для instrumentation тестов

`:sentry`

Клиент для отправки ошибок инфраструктуры и тестов в Sentry

`:signer`

Gradle Plugin для делегирования подписи apk/bundle inhouse сервису

`:slack`

Клиент для удобной публикации нотификаций в slack

`:statsd`

Клиент для отправки метрик через statsd

`:teamcity`

Клиент для работы с Teacmity: обертка для [REST API](https://github.com/JetBrains/teamcity-rest-client) 
и [service messages](https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ServiceMessages)

`:test-okhttp` 

Утилиты для тестирования при помощи [okhttpmockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)

`:test-project`

Утилиты для тестирования при помощи [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html)

`:time`

API для работы со временем в плагинах и android приложении

`:trace-event`

Клиент для работы с trace event format

- [Specification](https://docs.google.com/document/d/1CvAClvFfyA5R-PhYUmn5OOQtYMH4h6I0nSsKchNAySU/preview)
- [Trace-Viewer](https://github.com/catapult-project/catapult/tree/master/tracing)

`:utils`

TODO: разобрать по модулям

## Публикация модулей инфраструктуры

Настроены два типа публикации: 
1. [Bintray](https://bintray.com/avito-tech/maven/avito-android), которая миррорится в jcenter
2. И публикация в [inhouse artifactory](http://links.k.avito.ru/androidArtifactory) для проверки неопубликованной версии в интеграции с проектом

### Публикация из Teamcity

В [проекте](http://links.k.avito.ru/androidTeamcity) настроены две соответствующие конфигурации.

Публикация в bintray осуществляется вручную\
Используется `./publish.sh`

В Artifactory дается возможность указать версию при публикации\
Используется `./publish_local.sh`

### Gradle

`./gradlew publishToBintray`

Должны быть доступны env:
- `BINTRAY_USER`
- `BINTRAY_API_KEY`

`./gradlew publishToArtifactory`

Должны быть доступны env:
- `ARTIFACTORY_URL`
- `ARTIFACTORY_USER`
- `ARTIFACTORY_PASSWORD`

Версию можно указать с помощью:\
env `PROJECT_VERSION` или gradle property `projectVersion`
