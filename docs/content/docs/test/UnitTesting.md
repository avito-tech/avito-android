---
title: Unit tests
type: docs
---

# Unit tests

[How to write good tests (mockito)](https://github.com/mockito/mockito/wiki/How-to-write-good-tests)

## Assertions

Рекомендуется использовать `assertThat()` вместо `assertEquals()` и т.п.

### Мотивация

#### Типизация

`assertEquals("id", 1L)` - падает только в рантайме
`assertThat("id", 'is'(1L))` - проверка при компиляции

#### Читаемость

assert "equals 3 x"
assert "x is 3"  - естественный порядок

#### Комбинирование условий

 `either(s).or(not(t))`
 `each(s)`
 И т.п. особенно с кастомными матчерами.

#### Сообщения об ошибках

```java
assertTrue(response.contains("color") || response.contains("colour"))

// java.lang.AssertionError
//  at org.junit.Assert.fail(Assert.java)
//  at org.junit.Assert.assertTrue(...)
// и т.п., никаких подробностей
```

```java
assertThat(response, anyOf(containsString("color"),containsString("colour")))

// java.lang.AssertionError:
// Expected: (a string containing "color" or a string containing "colour")
//   but: was "..."
// Expected :(a string containing "color" or a string containing "colour")
// Actual   :"..."
```

## Matchers

### Создание

Для создания рекомендуется использвать фабричные методы:

```java
import org.hamcrest.Matchers.hasSize

assertThat(collection, hasSize(1))
```

_Мотивация:_ меньше завязываемся на детали реализации, внутренние классы библиотеки.

### Верификация

Необходимо проверять корректность использования Mockito. Для этого можно использовать `Mockito.validateMockitoUsage()` либо `MockitoJUnit`.
_Мотивация:_ нестабильные тесты. Нарушение контракта Mockito не приводит сразу к падению, но может влиять на другие тесты. Из-за параллельного запуска падать будут в произвольных местах.
