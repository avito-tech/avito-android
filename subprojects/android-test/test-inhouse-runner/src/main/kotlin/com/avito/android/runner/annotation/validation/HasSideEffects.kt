package com.avito.android.runner.annotation.validation

import org.junit.rules.TestRule

/**
 * Marker for non-hermetic test rules
 * They should be used only in e2e tests
 */
interface HasSideEffects : TestRule
