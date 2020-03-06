---
title: IDE Benchmarking
type: docs
---

# Бенчмарки IDE

Используем [Performance testing plugin](https://intellij-support.jetbrains.com/hc/en-us/articles/207241225-Performance-testing-plugin-in-PhpStorm).

## Как попробовать?

1. Установи плагин
1. Запусти **Help > Diagnostic > Execute Performance Scrip from File**\
Выбери готовый сценарий из /benchmarks/ide

## Как проверить гипотезу и измерить разницу?

Используем в ручном режиме:

1. Прогони бенчмарк несколько раз до внесения изменений в проект
1. Сохрани резульататы. Они отобразятся в диалоге после прогона бенчмарка
1. Внеси изменения в проект
1. Прогони бенчмарк несколько раз, сравни результаты.

## Что еще?

- Плагин умеет записывать [YourKit](https://www.yourkit.com/) snapshot с детальной информацией о всех вызовах методов.
