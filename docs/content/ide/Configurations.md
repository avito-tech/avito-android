---
title: IDE Custom configurations
type: docs
---

# Кастомные конфигурации

Шарим в проекте кастомные [run/debug конфигурации](https://www.jetbrains.com/help/idea/creating-and-editing-run-debug-configurations.html), чтобы все работало "из коробки".\
Так гарантируем одинаковые корректные настройки, случайно не сломается.

![](https://www.jetbrains.com/help/img/idea/2019.3/ij-edit-run-debug-configs.png)

- `avito`: собирает, устанавливает и запускает Авито.

- `avitoInstall`: собирает и устанавливает Авито, но не запускает автоматически приложение.\
Нужна из-за бага ["Default activity not found"](https://issuetracker.google.com/issues/139859267).

- `localCheck`: проверка всех этапов компиляции с учетом [импакт анализа]({{< ref "/ci/ImpactAnalysis.md" >}})

## Как расшарить конфигурацию?

[Sharing Run/Debug Configurations](https://www.jetbrains.com/help/idea/sharing-run-debug-configurations.html)
