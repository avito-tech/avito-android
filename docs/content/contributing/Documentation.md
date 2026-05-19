# Documentation

_If the documentation is not good enough, people will not use what you do._

## Style guide

### Write simply and to the point

Documentation is intended to solve problems. Thus, it has to be simple and clear.  
We can recommend several definitive guides on writing in general:

- The Elements of Style - William Strunk Jr.
- [Пиши, сокращай](https://book.glvrd.ru/) ([glvrd.ru](https://glvrd.ru/) - online checker)

Extra materials about technical writing:

- [Jetbrains - Как писать полезные технические тексты](https://youtu.be/8SPq-9kS69M)
- [Microsoft Writing Style Guide](https://docs.microsoft.com/en-us/style-guide/welcome/)
- [GitLab documentation styleguide](https://docs.gitlab.com/ee/development/documentation/styleguide.html)
- [What nobody tells you about documentation](https://www.divio.com/blog/documentation/)

### Use appropriate format for the problem

There is no universal documentation. 
It needs to be structured around problem and a level of knowledge:

|                        | Studying      | Working       |
| ---------------------- |:-------------:|:-------------:|
| Practical steps        | Tutorial      | How-to guide  |
| Theoretical knowledge  | Explanation   | Reference     |

It's ok to mix some types in one page:

```text
# Managing X

Here goes short _explanation_ of X for new users.
You can reference here to a detailed information.  
If I know this topic, I will skip it easily.

## Create a new X

1. Do ... <-- Short how-to guide
1. ...

## Move files to X

Select ...
```

#### Tutorial

Analogy: teaching a child how to cook

![](https://upload.wikimedia.org/wikipedia/commons/thumb/d/d6/Parents_and_their_kids_cook_healthy_and_tasty_meals_150321-A-ZT122-171.jpg/1599px-Parents_and_their_kids_cook_healthy_and_tasty_meals_150321-A-ZT122-171.jpg)

Naming: 

- Getting started with X
- Writing your first test

---

- Assist newcomers to get started
- Is learning-oriented
- Shows _how_ to do in form of a lesson

The tutorial declares a goal in the beginning. 
After it guides by a series of steps. Each step is:

- Easy to follow
- Robust and reliable
- Gives a positive feedback by an immediate result
- Has as minimum theory as possible

Examples:

- [Creating Web Applications with Flask](https://www.jetbrains.com/help/pycharm/creating-web-application-with-flask.html)

#### How-to guide

Analogy: a recipe

![](https://media.defense.gov/2019/Jun/10/2002142660/780/780/0/190605-F-FR885-023.JPG)

Naming:

- How to run tests in CI - 👍
- Running tests - 👎
- Tests in CI - 👎

---

- Ориентация на практический результат: как решить проблему.
- Ключевое отличие от tutorial: уже знакомы с продуктом.  
Может адресовать проблему, которую новички могут даже не сформулировать.
- Не объясняет концепции, может только ссылаться при необходимости.

- Читатель понимает чего хочет достичь, но не знает как.
- Шаги не такие надежные и повторяемые, читатель уже знаком с продуктом.
- Шаги более гибкие
- Описание не обязано быть полным, только то что нужно для проблемы.

#### Explanation

Analogy: an overview article

Naming: "Testing infrastructure in CI"

- Ориентация на понимание, не решает конкретных практических задач.  
Это самый свободный формат описания.
- Описывает концепции, поясняет контекст и историю развития
- Дает альтернативные подходы и мнения, поясняет мотивацию

#### Reference

Analogy: an article in Wikipedia

![](https://upload.wikimedia.org/wikipedia/commons/2/29/Anoplogaster_cornuta_skeletal_system-en.svg)

- Ориентация на информацию: описать как устроено, как работает.
- Отличие от tutorial, how to guide: не фокусируется на решении конкретных проблем, хотя и может приводить примеры использования.

- Структура продиктована кодом, повторяет его
- Описывает детали в точности как они работают.
- Объясняет ситуацию как есть, не уходит в дискуссии, мнения, инструкции. Такие отвлечения мешают понять как работает.

## Structure

Вся документация состоит из набора [markdown файлов]((https://daringfireball.net/projects/markdown/syntax)).  
Используем генератор статических сайтов [MkDocs](https://www.mkdocs.org/),
тема [MkDocs material](https://squidfunk.github.io/mkdocs-material/).

### Menu on the left

Меню редактируется в `docs/mkdocs.yml`

### Page links

- `[licencse](license.md)` - страница в этой же директории
- `[licencse](../about/license.md)` - относительный путь к файлу
- `[licencse](https://choosealicense.com/licenses){target=_blank}` - открыть в новой вкладке

???+ warning 
    Абсолютные ссылки официально не поддерживаются. 
    Они могут работать локально, но сломаться после деплоя документации.

Ссылка на параграф:

- `[licencse](#license)` - параграф внутри текущей страницы
- `[licencse](about.md#license)` - параграф внутри другой страницы

Ссылка на параграф формируется автоматически с помощью транслитерации (см. slugify в mkdocs.yml)

`## Отчет в BI` будет ссылкой `#otchet-v-bi`

Но ее можно кастомизировать:

`## Отчет в BI {#bi}` будет ссылкой `#bi`

Подробнее: [mkdocs internal links](https://www.mkdocs.org/user-guide/writing-your-docs/#internal-links)

### Internal links

The documentation is public but some links and services are not.  
In this case, define it clearly and in advance.

To be consistent we use a snippet:

<pre><code>--8&lt;--
avito-disclaimer.md
--8&lt;--</code></pre>

--8<--
avito-disclaimer.md
--8<--

Also, you have to replace internal links by links.k.avito.ru shortener. It is available only in Avito.

To get links statistics add `+` to it.

### Diagrams

Diagrams is a preferable way for schemes and processes.  
They are cheaper in maintenance. Text is the source of truth.  
You don't need an original image and can edit it in a text editor.

Use [mermaid](https://mermaid-js.github.io/mermaid/#/) code block:

<pre><code>
&#96&#96&#96mermaid
stateDiagram
    [*] --> A
    A --> B
    B --> [*]
&#96&#96&#96
</code></pre>

```mermaid
stateDiagram
    [*] --> A
    A --> B
    B --> [*]
```

[Live editor](https://mermaid-js.github.io/mermaid-live-editor/)

### Images

Images are good for illustrating an explanation.  
Use any public file storage for them. For example, a Github CDN:

![](https://user-images.githubusercontent.com/499192/57450172-1a955f80-725e-11e9-9fed-267179bdab15.gif)

### Hints

```tpl
??? "Collapsed by default"
    Here's some content. Nesting is working.  

???+ "Expanded"
    Here's some content. Nesting is working. 
    
    ??? success
        Content.
    
    ??? warning classes
        Content.
       
    ??? danger "Danger!"
        And more content again.
```

??? "Collapsed by default"
    Here's some content. Nesting is working.

???+ "Expanded"
    Here's some content. Nesting is working.

    ??? success
        Content.
    
    ??? warning classes
        Content.
       
    ??? danger "Danger!"
        And more content again.

### Tabs

```tpl
=== "Tab 1"
    First tab content

=== "Tab 2"
    Second tab content
```

=== "Tab 1"
    First tab content

=== "Tab 2"
    Second tab content

### Snippets

[Snippets](https://facelessuser.github.io/pymdown-extensions/extensions/snippets) are partial pages that can be inserted
into another pages.  
All custom snippets are in `docs/snippets` directory.

Usage:

<pre><code>--8&lt;--
file name
--8&lt;--</code></pre>

### Lists

#### Ordered vs Unordered lists

Only use ordered list for describing a series of steps to follow.

Do: 👍

```md
Run commands:

1. `cd dir`
1. `./prepare_data.sh`
1. `./publish_results.sh`
```

Don't: 👎

```md
There are two components in the system:

1. `Collector`
1. `Publisher`
```

## Language

### Interaction with UI

- Use bold text for describing menu items and `>` as separator.  
Open **Preferences > Build, Execution, Deployment > Debugger**
- Use exact words as shown in UI 

### Выделение терминов

Выделяй символом ` команды, параметры, значения, имена файлов и т.п.:

- Добавь префикс `Dagger` в импортах перед `Component`
- Подключи модуль `dagger`
- Введи `last` в поиск
- Добавь параметр `syncAvito` в файл `properties.ini`

Не надо выделять все названия технологий, компаний и т.п. 
Это нагружает текст, его труднее читать:

- Для `DI` используем `Dagger` 👎

## Changing documentation

### How to check changes locally

Run `make docs`. It will open documentation at localhost.  
You can edit markdown files on the fly. Changes out of this directory usually require a restart.

### How to upgrade MkDocs and theme

0. Check the build locally with `./ci/documentation/build.sh`;
0. Preview the site locally with `./ci/documentation/preview.sh` (or `make docs`) and click through
   the rendered pages to make sure nothing visually broke;
0. Push the change to `develop`. GitHub Actions workflow `Deploy docs` installs dependencies from
   `docs/requirements.txt` and publishes the site.

Checklist for upgrades:

- Search
- [Markdown extensions](https://facelessuser.github.io/pymdown-extensions): mermaid, ... See settings in mkdocs.yml.

[MkDocs troubleshooting](https://squidfunk.github.io/mkdocs-material/troubleshooting/)
