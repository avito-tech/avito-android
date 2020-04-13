---
title: IDE Benchmarking
type: docs
---

# Бенчмарки IDE

{{< avito >}}

Используем [Performance testing plugin](https://intellij-support.jetbrains.com/hc/en-us/articles/207241225-Performance-testing-plugin).

{{< hint warning>}}
Чтобы плагин работал корректно на macOS, необходимо разрешить Android Studio/IntelliJ IDEA доступ к функциям
Accessibility системы. Для этого:

1. Открываем **System Preferences > Security & Privacy**
1. Выбираем вкладку **Privacy**
1. В списке слева находим пункт **Accessibility**
1. Ставим галочку в списке справа для нужных приложений
{{< /hint >}}

## Как попробовать?

1. Установи плагин
1. Запусти **Help > Diagnostic > Execute Performance Script...**
1. Выбери готовый сценарий из `/benchmarks/ide` и вставь содержимое скрипта в открывшееся диалоговое
   окно (содержимое выпадающего списка **Please select scenario:** не имеет значения)

## Как проверить гипотезу и измерить разницу?

Используем в ручном режиме:

1. Прогони бенчмарк несколько раз до внесения изменений в проект
1. Сохрани результаты. Они отобразятся в диалоге после прогона бенчмарка
1. Внеси изменения в проект
1. Прогони бенчмарк несколько раз, сравни результаты.

## Что еще?

- Плагин умеет записывать [YourKit](https://www.yourkit.com/) snapshot с детальной информацией о всех вызовах методов.
