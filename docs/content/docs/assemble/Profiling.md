---
title: Profiling
type: docs
---

# Профилирование сборки

Чтобы посмотреть подробную информацию о скорости сборке попробуй разные инструменты, выбери наиболее простой.

## Build Speed window (AS 4+)

- Собери проект
- **View > Tool Windows > Build > Build Speed**

![](https://developer.android.com/studio/preview/features/images/build-speed-chart-wna.png)

## Gradle build scan

https://guides.gradle.org/creating-build-scans/

Запусти сборку с аргументом `--scan`.  
В конце лога будет ссылка на отчет.

![](https://guides.gradle.org/creating-build-scans/images/build_scan_page.png)

**Known issues:**

- Не приходит первое письмо для активации ссылки:  
напиши в help@gradle.com, приложи ссылку.
- `This build scan cannot be viewed. A permanent error occurred processing the data.`   
Скорее всего уперлись в лимит на размер скана. Нет способа проверить это заранее.   
Попробуй собрать что-то более мелкое.
- Завышено время исполнения задач ([#8630](https://github.com/gradle/gradle/issues/8630))   
Это видно по косвенным признакам. 
На графике задача завершается сразу после завершения другой задачи из этого же модуля.

## Avito build trace

[Build trace plugin]({{< ref "/docs/projects/BuildTrace.md" >}})

## Gradle profiler

В [режиме профилировщика](https://github.com/gradle/gradle-profiler/#profiling-a-build) умеет запускать внешние профилировщики.  
Попробуй разные, тут нет однозначного победителя.

```bash
gradle-profiler --profile async-profiler \
--project-dir . \
--warmups 1 \
--gradle-user-home ~/.gradle-profiler \
--output-dir profiler \
help
```

## Android Tracer

[mirror-goog-studio-master-dev/tracer/](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-master-dev/tracer/)

Легкий профилировщик от Google:

- Показывает исполнение кода из AGP внутри Gradle worker
- Можно разметить аннотациями свой код или указать в конфиге, что логировать

 
**Как использовать:**

- Запусти сборку с параметром `android.enableProfileJson=true`
- Открой файл `build/android-profile` в `chrome://tracing`

**Кастомная конфигурация:**

- Прочти README, есть неочевидные вещи.
(jar файл нельзя переименовывать и т.п.)
- Выбери профиль, в репозитории есть пара под разные сценарии.
- Прокинь агент в демон, подходит GRADLE_OPTS:   
`export GRADLE_OPTS="-javaagent:/path/to/trace_agent.jar=/path/to/deploy.profile"`
- Добавь в jvm args в gradle.properties:   
`org.gradle.jvmargs="-javaagent:/path/to/trace_agent.jar=/path/to/deploy.profile"`

Полезно посмотреть в образовательных целях, как пример профилирования с помощью [java agent](https://habr.com/ru/post/230239/)
