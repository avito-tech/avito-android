---
title: How to start
type: docs
---

# How to start

## Minimal requirements

- 16Gb RAM
- Linux/OSX/Windows

## 1. Установи Java

Поддерживаем только Java 8.

## 2. Установи Intellij IDEA или Android Studio

Поддерживаем последнюю стабильную версию, но обычно работает и beta, и canary.\
Для обновлений рекомендуется [JetBrains Toolbox](https://www.jetbrains.com/toolbox/).\
В одном приложении доступны все IDE.

## 3. Проверь проект

- Подключи VPN
- Запусти в корне проекта: `./gradlew help`     

Должно отработать успешно. Если упадет, прочитай текст ошибки и поправь.

## 3. Настрой IDE

### Обнови Kotlin плагин до последней версии

**Preferences > Languages & Frameworks > Kotlin**

### Подключи необходимые плагины

- Editorconfig - для [code style]({{< ref "/docs/contributing/CodeStyle.md" >}})

### Включи оптимизации в IDE

[Ускорение IDE]({{< ref "/docs/ide/Speedup.md" >}})
