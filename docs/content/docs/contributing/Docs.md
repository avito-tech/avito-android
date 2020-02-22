---
title: Documentation
type: docs
---

# Documentation

_If the documentation is not good enough, people will not use what you do._

## Style guide

### Write simply and to the point

Documentation is intended to solve problems. Thus, it has to be simple and clear.\
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

{{< columns >}}

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

<--->

#### How-to guide

Analogy: a recipe

![](https://media.defense.gov/2019/Jun/10/2002142660/780/780/0/190605-F-FR885-023.JPG)

Naming:

- How to run tests in CI - 👍
- Running tests - 👎
- Tests in CI - 👎

---

- Ориентация на практический результат: как решить проблему.
- Ключевое отличие от tutorial: уже знакомы с продуктом.\
Может адресовать проблему, которую новички могут даже не сформулировать.
- Не объясняет концепции, может только ссылаться при необходимости.

- Читатель понимает чего хочет достичь, но не знает как.
- Шаги не такие надежные и повторяемые, читатель уже знаком с продуктом.
- Шаги более гибкие
- Описание не обязано быть полным, только то что нужно для проблемы.

{{< /columns >}}

{{< columns >}}

#### Explanation

Analogy: an overview article

Naming: "Testing infrastructure in CI"

- Ориентация на понимание, не решает конкретных практических задач.\
Это самый свободный формат описания.
- Описывает концепции, поясняет контекст и историю развития
- Дает альтернативные подходы и мнения, поясняет мотивацию

<--->

#### Reference

Analogy: an article in Wikipedia

![](https://upload.wikimedia.org/wikipedia/commons/2/29/Anoplogaster_cornuta_skeletal_system-en.svg)

- Ориентация на информацию: описать как устроено, как работает.
- Отличие от tutorial, how to guide: не фокусируется на решении конкретных проблем, хотя и может приводить примеры использования.

- Структура продиктована кодом, повторяет его
- Описывает детали в точности как они работают.
- Объясняет ситуацию как есть, не уходит в дискуссии, мнения, инструкции. Такие отвлечения мешают понять как работает.

{{< /columns >}}

## Structure

Вся документация состоит из набора [markdown файлов]((https://daringfireball.net/projects/markdown/syntax)).\
Используем генератор статических сайтов [Hugo](https://gohugo.io/) (тема - [Book](https://themes.gohugo.io/hugo-book/)).\
Для проверки стиля - [markdownlint](https://github.com/markdownlint/markdownlint/blob/master/docs/RULES.md#rules)    

### Menu on the left

Меню редактируется в `docs/content/menu/index.md`

### Links

Абсолютная ссылка: `{{</* ref "/Name.md" */>}}`, где путь относителен директории `content/`.   
Относительная ссылка: `{{</* relref "Name.md" */>}}`.   
Anchor: `{{</* ref "/Name.md#header" */>}}`.

Подробнее: [cross references](https://gohugo.io/content-management/cross-references/)

### Internal links

Документация публичная, поэтому в ней ней не должно быть прямых ссылок на внутренние сервисы (issue tracker, CI, ...).\
Чтобы не терять эту информацию, используй links.k.avito.ru для редиректа.

{{< hint info>}}
Ссылки с кастомным путем должны указывать на что-то неизменяемое.\
Путь изменится, а ссылку уже не подменить.
{{< /hint >}}

Чтобы посмотреть статистику по ссылке, добавь к ней `+`.

### Diagrams

Diagrams is a preferable way for schemes and processes.\
They are cheaper in maintenance. Text is the source of truth.\
You don't need an original image and can edit it in a text editor.

Use [mermaid](https://mermaid-js.github.io/mermaid/#/) shortcode:

[Live editor](https://mermaid-js.github.io/mermaid-live-editor/)

```tpl
{{</*mermaid*/>}}
stateDiagram
    [*] --> A
    A --> B
    B --> [*]
{{</*/mermaid*/>}}
```

{{<mermaid>}}
stateDiagram
    [*] --> A
    A --> B
    B --> [*]
{{</mermaid>}}

### Images

Images are good for illustrating an explanation.\
Use any public file storage for them. For example, a Github CDN:

![](https://user-images.githubusercontent.com/499192/57450172-1a955f80-725e-11e9-9fed-267179bdab15.gif)

There is a html [figure](https://gohugo.io/content-management/shortcodes/#figure) element for more control. 

### Hints

```tpl
{{</* hint [info|warning|danger] */>}}
**Markdown content**  
Lorem markdownum insigne. Olympo signis Delphis!
{{</* /hint */>}}
```

{{< hint info>}}
**Markdown content**  
Lorem markdownum insigne. Olympo signis Delphis!
{{< /hint >}}

### Buttons

```tpl
{{</* button relref="/" */>}}Home{{</* /button */>}}
{{</* button href="http://repo/CONTRIBUTING.md" */>}}Contribute{{</* /button */>}}
```

{{< button relref="/" >}}Home{{< /button >}}
{{< button href="http://repo/CONTRIBUTING.md" >}}Contribute{{< /button >}}

### Tabs

```tpl
{{</* tabs "Unique ID" */>}}
{{</* tab "MacOS" */>}} MacOS Content {{</* /tab */>}}
{{</* tab "Linux" */>}} Linux Content {{</* /tab */>}}
{{</* tab "Windows" */>}} Windows Content {{</* /tab */>}}
{{</* /tabs */>}}
```

{{< tabs "Unique ID" >}}
{{< tab "MacOS" >}} MacOS Content {{< /tab >}}
{{< tab "Linux" >}} Linux Content {{< /tab >}}
{{< tab "Windows" >}} Windows Content {{< /tab >}}
{{< /tabs >}}

### Columns

```tpl
{{</* columns */>}}
Left Content
<--->
Right Content
{{</* /columns */>}}
```

{{< columns >}}
Left Content
<--->
Right Content
{{< /columns >}}


### Expand

```tpl
{{</* expand "Title" */>}}
Markdown content
{{</* /expand */>}}
```

{{< expand "Title">}}
Markdown content
{{< /expand >}}

### Math

[KaTeX](https://katex.org/)

```tpl
{{</* katex */>}}
\overline{a \lor b} = \overline{a} \land \overline{b}
{{</* /katex */>}}

```

{{< katex >}}
\overline{a \lor b} = \overline{a} \land \overline{b}
{{< /katex >}}

### Lists

#### Нумерованный или ненумерованный

Используй нумерованный список только если он описывает последовательность шагов.

{{< columns >}}
👎

```md
Система состоит из следующих компонентов:
1. `Collector`
1. `Publisher`
```

<--->
👍

```md
Запусти команды:
1. `cd dir`
1. `./prepare_data.sh`
1. `./publish_results.sh`
```

{{< /columns >}}

## Language

### Interaction with UI

- Use bold text for describing menu items and `>` as separator\
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

### How to check locally

Run: `docs/local.sh`\
It will open documentation at `localhost:1313`.\
You can edit markdown files in the `content/` on the fly. Changes out of this directory usually require a restart.  

Checklist for changes:

- Search
- Shortcodes: mermaid, ...

[Hugo troubleshooting](https://gohugo.io/troubleshooting/faq/)

### Publishing changes

Run manually after changes: `docs/publish.sh`\
Auto-publish: MBS-7514.

### How to update hugo

1. Обнови версию hugo в Dockerfile
1. Обнови другие файлы
    - Проверь, что локально установлена такая-же версия: `hugo version`
    - Сгенерируй новый сайт: `hugo new site template`
    - Сравни директорию `docs` со сгенерированным пустым сайтом

### How to update theme

Скачай новую версию темы и подмени существующие файлы.\
Мы кастомизировали тему, проверь что не потеряем эти изменения:
- Удалили примеры (book/exampleSite) и статику для них (book/images/)
- Удалили js файлы для shortcode (book/static/*.js), потому что они могут весить мегабайты. 
Загружаем из unpkg.com.
