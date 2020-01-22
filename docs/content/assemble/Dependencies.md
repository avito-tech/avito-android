---
title: Dependencies
type: docs
---

# Зависимости

## Как подключить внешнюю зависимость?

Все зависимости объявляем в одном файле - Dependencies.kt.\
На них ссылаемся в build.gradle модуля:

```groovy
dependencies {
    implementation(Dependencies.supportAnnotations)
```

## Как зафорсить версию зависимости?

Конфигурация проекта упадет, если в транзитивных зависимостях прилетают разные версии одной и той-же библиотеки:

```none
> Conflict(s) found for the following module(s):
       - com.google.android.gms:play-services-measurement-api between versions 17.2.1 and 17.0.0
Run with:
     --scan or
     :avito:dependencyInsight --configuration debugRuntimeClasspath --dependency com.google.android.gms:play-services-measurement-api
     to get more insight on how to solve the conflict.
```

В приложении должна быть только одна версия.\
Выбери подходящую версию (обычно берем старшую) и добавь зависимость в `Dependencies`.\
Автоматически форсим версии для всех зависимостей из `Dependencies` (см. `applyDefaultResolutionStrategy`).
