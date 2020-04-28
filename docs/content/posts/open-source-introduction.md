---
date: 2019-12-26
title: Open source
tags: [open-source]
---

# Open source: CI/CD and test infrastructure for Android

Avito.ru is the biggest Russian classified. 
We have moved our Android infrastructure into open source: Gradle plugins, emulators, and test libraries. 
[Our code](https://github.com/avito-tech/avito-android) will be useful in automating CI/CD and will also facilitate the coding and support of autotests. 
 
In this review, we will explain why we decided to move into open source, present the central libraries of the project, and suggest whom to contact with any questions. 
We will analyze in detail the individual libraries, Gradle plugins, and our development approaches in future posts.
 
![ijhasr2dv95azaem9mgilu7e1kw](https://user-images.githubusercontent.com/1104540/80410956-0cf44200-88d4-11ea-879d-cd9b6acd29af.png)

## Who we are and what we do

We are developing solutions for Android as part of the Speed platform team. There are four of us:

{{< columns >}}

Sergey Boishtyan\
Senior engineer\
[sboishtyan](https://twitter.com/sboishtyan)

![](https://user-images.githubusercontent.com/1104540/80411459-e8e53080-88d4-11ea-9751-1ed675da9f25.jpg)

<--->

Dima Voronin\
Lead engineer\
[DmitriVoronin](https://twitter.com/DmitriVoronin)

![](https://user-images.githubusercontent.com/1104540/80411540-074b2c00-88d5-11ea-8087-ddbab5d85dd3.jpg)

<--->

Eugene Krivobokov\
Senior engineer\
[eugene_kr](https://twitter.com/eugene_kr)

![](https://user-images.githubusercontent.com/1104540/80413105-9a856100-88d7-11ea-9e07-b5a5e48882cf.png)

<--->

Daniil Popov\
Senior engineer\
[Int02h](https://twitter.com/int02h)

![](https://user-images.githubusercontent.com/1104540/80411864-8d677280-88d5-11ea-8a91-4f40d3eec71d.jpg)

{{< /columns >}}
 
We are in charge of delivering changes in all Avito’s Android apps to users as fast as possible. Our area of responsibility covers:

- Local project activities: making sure that everything is quickly built and the IDE runs fast.
- CI pipeline: tests and all possible checks.
- CD: tools for release engineers. 

## Why open source

We wanted not only to mirror the code in the open source repository on GitHub but also to learn something new and make a contribution to the software engineering community. 
There were five key reasons to move the project into open source:

- Get feedback.
- Influence industry standards.
- Learn something new.
- Influence third-party libraries.
- Promote our personal brands.
 
Let's discuss these one by one.

## Get feedback and make the code easier to reuse

We do the tooling for Avito’s engineers, and our users need all solutions to simply work. 
We lack the outsider perspective of developers who work on similar problems. 
We need them to point out issues in the internal implementation and the convenience of connecting to our project.

We have already seen how moving the code to GitHub highlighted the problems of reuse. 
When you understand that other companies can use your project, you start looking at the architecture differently. 
Reusing code is not an end in itself. But this external criterion says a lot about the quality of the architecture and its flexibility.

### Influence the industry standards

We have been developing infrastructure for mobile apps since 2017 and regularly talk about this at conferences and events.

In addition to talking about this, we always wanted to share the code and allow others to reuse it. 
Indeed, many Android developers face similar challenges:

- How to author effective autotests.
- How to run these in pull requests.
- How to maintain infrastructure cost-effectively.
 
There are no generally accepted, universal solutions for these tasks — each company solves them in its way. 
We share our best practices so that developers of new projects do not have to collect bit by bit information on testing mobile apps and building CI/CD. 
We want to offer ready-made solutions for routine problems instead of having to invent the wheel. 
And even if nobody uses the project code in its original form, developers will be able to see our approaches and improve their libraries.

### Learn by teaching

Just moving the code into open source is not enough. 
Practices, approaches, methods of troubleshooting, and making decisions — this is what is essential. 
Sharing these, we verify whether our ideas and ready-made solutions work outside of Avito.

### Influencing third-party libraries and fixing their problems faster

Imagine you are facing a problem in Android or a library and cannot find a workaround. 
You need help from the community or the code’s authors. 
You asked a question on Stack Overflow, filed a bug report in Google IssueTracker, described everything in detail, but the problem won’t reproduce. 
You are asked to share a test case. All this takes extra time.
 
Open source helps you create a reproducible example faster. 
We have a test app, which uses part of the infrastructure. Its main function is dogfooding, i.e. making sure as early 
as possible using a simple and isolated case that everything works. 
But this same app makes it easier to demonstrate bugs. When we show a reproducible example in a third-party library, 
it becomes easier for its developer to understand what is going on. This increases the chances that the developers will fix the issue. 

The popularity of an open source project also increases the likelihood that you will be paid attention to. 
When an issue in a library has many stars and users, this increases the pressure, and the issue becomes more difficult to ignore. 
Achieving this without open source is more challenging — the apps has to be super popular, or one should make oneself known.

### PR and personal motivation

Last but not least, is personal benefit. Everybody benefits when their daily work gains publicity. 
Avito grabs public attention by offering a useful product, and we promote our personal brands as engineers and develop additional motivation for working. 
We no longer need to use our free time to work on our own projects or commits in third-party open source libraries. 

## What’s in the open source

We have made available [in the GitHub repository](https://github.com/avito-tech/avito-android) almost all of our Android and CI/CD testing infrastructure. 
To make it easier for other developers to navigate the project, we grouped all its modules by function:

- Gradle plugins. 
- Libraries for Android testing modules. 
- Emulators.
 
Let’s discuss some of the most important libraries.

### Test runner

This is a Gradle plugin to run instrumentation tests. 
The closest analog is [Marathon](https://github.com/Malinskiy/marathon), but our plugin runs only under Android.
 
[Test runner:]({{< ref "/docs/test/Runner.md" >}})

- Specifies which tests to run. Filtering by annotations, by packages, by the results of the last run is supported.
- Specifies which emulators to run tests on. Backs these up to Kubernetes or connects to local emulators.
- Sets test restart conditions.
- Sends a final report with the test run results.

The results are stored in custom TMS (test management system), which is not open source. 
We are working on the possibility of connecting to a different implementation.

### Impact analysis

We have about 1,600 instrumentation tests and 10K unit tests. We would like to run all the tests for any code change,
but this is not possible — such a test run would take too much time.
 
A simple solution is to manually subdivide the tests into subsets, for example, smoke tests, fast, slow tests, and run only one set at a time. 
But with this approach, there is always a risk of missing an error, because it is not clear which set of tests is optimal. 
An ideal solution would be to understand which minimum test set can verify all changes. 
This is known as [test impact analysis](https://martinfowler.com/articles/rise-test-impact-analysis.html).
 
We wrote [a Gradle plugin]({{< ref "/docs/ci/ImpactAnalysis.md" >}}), which searches for changes in modules, parses tests, and determines which ones to run.
 
For more details of the main modules and approaches, see [the project documentation]({{< ref "/docs/Infrastructure.md#buildscript-dependencies" >}}).
It is still incomplete, and not everything is translated. We want to make the documentation easier to understand, and need your help. 
Tell us what to improve and correct in the documentation in [our Telegram chat](https://t.me/avito_android_opensource_en).

## How our libraries can be useful

Since there are many components in our project, its applications depend on your needs. 
If you are working on a similar problem or just want to understand the technology better — feel free to contact us in our 
Telegram chat ([En](https://t.me/avito_android_opensource_en), [Ru](https://t.me/avito_android_opensource)). 
We will share what we know, try to help, and show relevant examples.
 
You can ask anything:
- How do we handle unstable tests?
- Why so much code? It makes no sense.
- Why is all the code in Gradle plugins and not in python scripts?
 
If you want to use a specific module, you can try it in [the test app](https://github.com/avito-tech/avito-android/tree/develop/subprojects/android-test/test-app). 
Currently, it shows an example of using our test runner.
 
Unfortunately, we still have few examples of reuse in other projects, so integration may reveal yet unknown limitations. Let us know if this happens, and we we will see what to fix.

## Conclusion

In our upcoming posts, we plan to talk about:

- Our test runner.
- Test anatomy — what happens from the moment of clicking “Run” in the IDE up to the test completion moment.
- How we deal with the instability of tests and infrastructure.
- Our approaches to writing infrastructure.
- How we reduced the release time from month to week.
 
We want to discuss some more general topics, too:

- How to start writing tests.
- Fundamentals of testing for beginners — common approaches and technologies.
 
Comment to let us know what you would like to read about. So we will know which topic to cover first.
