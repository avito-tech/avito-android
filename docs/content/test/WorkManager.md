---
title: Work Manager Testing
type: docs
---

# Work Manager Testing

This documents describes how to test WorkManager's Workers.

## WorkManager is disabled for tests

Because using real WorkManager while testing involves a lot of problems as it starts to initialize 
just before `AvitoTestApp` (using ContentProvider mechanism). Sometimes it leads to a classic 
race condition: some Worker tries to get its dependencies to build its own Dagger graph but 
Application isn't ready so test runner ends up with a crash.

## How to test WorkManager's Workers?

The best way to test your Workers is the way [described](https://developer.android.com/topic/libraries/architecture/workmanager/how-to/testing-210) 
in official documentation. In short, you can test your Workers directly without initializing WorkManager.

