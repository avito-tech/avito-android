---
title: Введение
type: docs
---

# Запуск тестов

## IDE

Обрати внимание на [кастомные конфигурации]({{< ref "/ide/Configurations.md" >}})

### Robolectric

Просто запускаем стрелками Run напротив имени тестового класса или метода.

Важно чтобы в настройках запуск был делегирован Gradle:

`Preferences > Build, Exceution, Deployment > Build Tools > Gradle`

{{< tabs "robolectric" >}}
{{< tab "AS 3.6+" >}}
Build and run using: Gradle
Run tests using: Gradle
{{< /tab >}}
{{< tab "AS 3.5" >}}
- Delegate IDE build/run actions to Gradle
- Run tests using: Gradle Test Runner
{{< /tab >}}
{{< /tabs >}}

### Instrumentation

#### Known issues

- Не работает запуск конкретного метода в тесте, только всего класса ([#127662898](https://issuetracker.google.com/issues/127662898))

## CI

[Кастомный запуск]({{< relref "DynamicConfig.md" >}})
