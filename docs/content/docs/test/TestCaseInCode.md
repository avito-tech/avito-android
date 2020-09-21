---
title: Test case in code
type: docs
---

{{<avito page>}}

# Test case in code

Avito uses in-house TMS: [Internal docs](http://links.k.avito.ru/h)

TMS was a source of truth about test cases in avito since the very early days.\
While company shifted to test automation, manual synchronization between test cases in TMS and code in autotests became a problem.\

To simplify this process "test cases in code" concept was born.\
Now only code contains truth about test cases, and automated ones are read-only in TMS.

## How to synchronize your test with TMS?

Test should have this minimal set of annotations:
 - `@ExternalId(UUID)` UUID - random generated uuid (use any tool, for example online generator)
 - `@Description` - testcase title
 - `@FeatureId(IntArray)` - specify test's place in global features tree
 - `@TagId(IntArray)` - specify id's of team tag cloud
 - [Kind]({{< ref "#kind" >}})
 
-- deprecated --
 - `@CaseId(Int)` - TMS id, consider using [test case in code]({{< ref "#test-case-in-code" >}})

### Kind

Kind has to be specified to map tests on test pyramid in TMS, see `com.avito.report.model.Kind`

These annotations also used to filter tests for different suites.
 - @E2ETest - e2e functional tests
 - @UIComponentTest - UI tests without(or minimal) e2e networking
 - @IntegrationTest - Instrumentation tests without UI
 - @UnitTest - Classical unit tests that should be synced with TMS
 - [@ManualTest]({{< ref "#stubs-tests-without-implementation" >}})
 - [@UIComponentStub]({{< ref "#stubs-tests-without-implementation" >}})
 - [@E2EStub]({{< ref "#stubs-tests-without-implementation" >}})
 - [@ScreenshotTest]({{< ref "/docs/test/ScreenshotTesting.md" >}})

## When will my tests appear in TMS?

After test with required annotations merged, next full suite test run will trigger sync process.

[Internal Teamcity configuration](http://links.k.avito.ru/androidnightly)

This build runs every night, and it is recommended not to run this build manually only to sync a bunch of tests, because it's fairly heavy.

## How to find out why a test was not synced

[Let us know]({{< ref "/docs/Contacts.md" >}}) if something seems to go wrong.

Please attach:
 - your pull request link
 - nightly build that should trigger sync link
 - test case id link in TMS (if it was edited, not a new one)

## Stubs: tests without implementation

If you need to sync tests, but you're not ready to automate it, there is a way: stubs.\
Stubs are tests with all needed meta information (annotations, steps), but without actual implementation and asserts. 

To differentiate stubs from regular tests additional kind annotations added\
Kind `Manual` are special one, to express intention that there are no plans to automate this case.

### Stubs generation

To get help with moving test cases, check internal module's `:test:generator` readme. \
This project will generate test stubs from TMS id's.

## How synchronization works

{{<mermaid>}}
sequenceDiagram
    InstrumentationPlugin->>ReportService: addTest() X times for whole suite
    InstrumentationPlugin->>ReportService: setFinished()
    ReportService->>TmsEventProcessor: event
    loop analyze
        TmsEventProcessor->>TmsEventProcessor: just regular test run, skip
    end
    TmsPlugin->>ReportService: pushPreparedData(<This is source of truth with timestamp>)
    TmsPlugin->>ReportService: setFinished()
    ReportService->>TmsEventProcessor: event
    loop analyze
        TmsEventProcessor->>TmsEventProcessor: contains fresh(newest date) source of truth
    end
    loop analyze
        TmsEventProcessor->>TmsEventProcessor: parse report and prepare payload for TMS
    end
    TmsEventProcessor->>TMS: sendModifiedTestSuite()
{{</mermaid>}}

## TMS Gradle plugin

Project should apply and configure the plugin:

```kotlin
plugins {
    id("com.avito.android.tms")
}

tms {
    reportsHost.set("<report viewer host>")
}
```

And also add a [CI step]({{< ref "/docs/projects/CiSteps.md" >}})

```kotlin
builds {
    release {  // build that contains full test suite
        markReportAsSourceForTMS {
            configuration = "ui" // instrumentation configuration to wait for
        }
    }
}
```

## How to troubleshoot sync issues?

Nightly build could be skipped if there are no code changes, keep it in mind while troubleshooting. 

Nightly build log should contain line: 
`[TMS] Test suite for tms version <timestamp>, with id: <id>, coordinates: <reportCoordinates> marked as source of truth for tms`

Check `[TMS]` tag for possible errors

Look at TMS sync service metrics:
 - [Test case count](http://links.k.avito.ru/androidtmscount) (check `aa/avito-android.total`)
 - [Create/Delete events](http://links.k.avito.ru/androidtmsevents) (check `aa/avito-android.created` and `aa/avito-android.deleted`)
