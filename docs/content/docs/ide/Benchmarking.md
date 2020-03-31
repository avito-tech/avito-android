---
title: IDE Benchmarking
type: docs
---

# Бенчмарки IDE

Используем [Performance testing plugin](https://intellij-support.jetbrains.com/hc/en-us/articles/207241225-Performance-testing-plugin).

{{< hint warning>}}
К сожалению в Android Studio 3.6.1 (Build #AI-192.7142.36.36.6241897, built on
February 27, 2020) и IntelliJ IDEA 2019.3.3 (Build #IC-193.6494.35 February 11, 2020) Performance
testing plugin не работает должным образом. А именно команда `%[ENTER]` не делает ничего: не
переводит строку и не выбирать опции в выпадающем меню автокомплита.

К самому плагину оставлен
[комментарий](https://intellij-support.jetbrains.com/hc/en-us/articles/207241225/comments/360001111259),
а также заведен [issue](https://intellij-support.jetbrains.com/hc/en-us/requests/2577588) в трекер
IntelliJ.
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
