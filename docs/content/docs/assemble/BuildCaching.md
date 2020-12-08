---
title: Build caching
type: docs
---

{{<avito page>}}

Здесь описано как работает кеширование сборок.

# Кеширование

## Архитектура

### Remote cache

Все сборки используют кеширование, читают из remote кеша.

{{<mermaid>}}
graph TB
    build_local["Локальная<br/>сборка"]-- read -->cache["Remote<br>cache"]
    ci_build[CI<br/>сборка]-- read/write -->cache
{{</mermaid>}}

См. настройки buildCache в корневом settings.gradle:

```kotlin
buildCache {
    local { ...}
    remote<HttpBuildCache> { ... }
}
```

Образ: [gradle cache node](https://github.com/avito-tech/avito-android/tree/develop/ci/docker/gradle-cache-node).\
За основу взят официальный образ [Gradle build-cache-node](https://hub.docker.com/r/gradle/build-cache-node).

Пишем в кеш только из сборок в CI, т.к. контролируем полностью окружение.

Для локальных сборок прогреваем кеш в CI, имитируем локальную сборку: 
[Remote build cache warmup](http://links.k.avito.ru/Tx)

### Локальный кеш

Локальный кеш работает как прокси.

{{<mermaid>}}
sequenceDiagram
    task->>/build: up to date?
    /build-->>task: нет
    task->>local cache: есть в локальном кеше?
    local cache-->>task: нет
    task->>remote cache: есть в remote кеше?
    remote cache->>task: да
    task->>local cache: копируем в локальный кеш
    task->>/build: копируем в результаты
{{</mermaid>}}

Подробнее про устройство кеширования: [Build cache](https://docs.gradle.org/current/userguide/build_cache.html)

### Метрики

TBD

### Алерты

Cм. `ci/alerts/`

## Troubleshooting

### Remote cache miss в локальной сборке

Здесь описана краткая версия гайда [build cache debugging](https://docs.gradle.org/nightly/userguide/build_cache_debugging.html).
Продублировали чтобы показать процесс без Gradle Enterprise.

Смотри также [common caching problems](https://docs.gradle.org/nightly/userguide/common_caching_problems.html).

#### Симптомы

Общее количество промахов: вкладка **Performance > Build cache**
![remote-cache-misses](https://user-images.githubusercontent.com/1104540/100124960-4f432900-2e8d-11eb-8efa-a84d8675928c.png)

Список всех задач: вкладка **Timeline**

- Task output cacheability: Cacheable
- Outcome: SUCCESS

![tasks](https://user-images.githubusercontent.com/1104540/100125051-66821680-2e8d-11eb-8b70-27c493b493ac.png)

Детали по конкретной задаче:

![task](https://user-images.githubusercontent.com/1104540/100125446-dabcba00-2e8d-11eb-88e1-afda77793072.png)

#### Диагностика

Проверяем на одном коммите.

1. Прогреть кеш с детальным логами `-Dorg.gradle.caching.debug=true`\
См. [Remote build cache warmup](http://links.k.avito.ru/Tx)

2. Собрать локальную сборку, исключив локальный кеш и инкрементальную сборку

```sh
rm -rf ~/.gradle/caches/build-cache-1
rm -rf .gradle/build-cache

./gradlew clean --quiet

./gradlew --stop

./gradlew help

./gradlew ... -Dorg.gradle.caching.debug=true --scan
```

См. пример в `benchmarks/scripts/check_remote_caching.sh`.

3. Посмотреть build scan, какие задачи не взяты из кеша.

![tasks](https://user-images.githubusercontent.com/1104540/100125051-66821680-2e8d-11eb-8b70-27c493b493ac.png)

4. Выбрать задачу для сравнения.\
Проще сначала взять ту, которая запускалась как можно раньше. У нее будет меньше входных данных.

5. Найти по логам разницу в input'ах между двумя запусками:

```text
CI:

> Task :module:compileKotlin
Appending input file fingerprints for 'source' to build cache key: 7023b2220d8c42b5ee69b8b0af28b52e - RELATIVE_PATH{...}
Appending input file fingerprints for 'classpath' to build cache key: 7023b2220d8c42b5ee69b8b0af28b52e - CLASSPATH{...} <---
Build cache key for task ':module:compileKotlin' is ed6b6082714ccd0b4e8472884f055df0

Локальная сборка:

> Task :module:compileKotlin
Appending input file fingerprints for 'source' to build cache key: 7023b2220d8c42b5ee69b8b0af28b52e - RELATIVE_PATH{...}
Appending input file fingerprints for 'classpath' to build cache key: 1252b653d16b1cfb55956c0cf84058dc - CLASSPATH{...} <---
Build cache key for task ':module:compileKotlin' is 12f0ae79f405c46e9045f83b66543728
```

"Build cache key for task" - output задачи, виден в build scan.\
"input file fingerprints for 'xxx' to build cache key" - input.

Найти input'ы задачи можно в ее исходниках.

6. Для найденной разницы сравни что именно отличается.

При сравнении помни, что в Gradle есть много оптимизаций для более умного сравнения файлов:

- Учитывать ли путь только относительно проекта (`RELATIVE_PATH` в логах)
- Учитывать только ABI от классов
- [Нормализация](https://docs.gradle.org/nightly/userguide/build_cache_concepts.html#normalization) input'ов

{{< hint info >}}
Когда знаем конкретную задачу, проверять изменения можем без очистики кеша.
`org.gradle.caching.debug` всегда логирует input/output. 

```text
./gradlew :module:compileKotlin -Dorg.gradle.caching.debug=true

> Task :module:compileKotlin UP-TO-DATE
Appending implementation to build cache key: org.jetbrains.kotlin.gradle.tasks.KotlinCompile_Decorated@2f0174769b8ce32d370ff13bae07baee
Appending additional implementation to build cache key: org.jetbrains.kotlin.gradle.tasks.KotlinCompile_Decorated@2f0174769b8ce32d370ff13bae07baee
...
Appending input file fingerprints for 'source' to build cache key: 7023b2220d8c42b5ee69b8b0af28b52e - RELATIVE_PATH{...}
Appending input file fingerprints for 'classpath' to build cache key: 7023b2220d8c42b5ee69b8b0af28b52e - CLASSPATH{...}
Build cache key for task ':module:compileKotlin' is ed6b6082714ccd0b4e8472884f055df0
```

{{< /hint >}}

7. Что еще может помочь?

- Поищи известные проблемы по типу задачи в issue tracker'е.
- На input'ы влияет и окружение: [common caching problems](https://docs.gradle.org/nightly/userguide/common_caching_problems.html)
- Примеры старых проблем с кешированием: MBS-9988, MA-2069
- [Gradle community Slack](https://gradle-community.slack.com/) - канал #caching.

## Known issues

- [KT-27687](https://youtrack.jetbrains.com/issue/KT-27687) - удаляем пустые директории в исходниках автоматически. См. `git_hooks/post-checkout`.
- [Java version tracking](https://avito-tech.github.io/avito-android/docs/projects/buildchecks/#java-version) - фиксируем мажорную версию Java.
- [android.jar](https://avito-tech.github.io/avito-android/docs/projects/buildchecks/#android-sdk-version) - фиксируем ревизию build tools.
