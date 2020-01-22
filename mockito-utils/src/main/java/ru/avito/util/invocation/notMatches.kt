package ru.avito.util.invocation

import org.mockito.internal.invocation.InvocationMatcher
import org.mockito.invocation.Invocation

fun InvocationMatcher.notMatches(invocation: Invocation) = matches(invocation).not()