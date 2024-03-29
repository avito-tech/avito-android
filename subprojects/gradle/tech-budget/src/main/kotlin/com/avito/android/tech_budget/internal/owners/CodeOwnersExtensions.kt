package com.avito.android.tech_budget.internal.owners

import com.avito.android.CodeOwnershipExtension
import com.avito.android.OwnerSerializerProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

internal fun Project.requireCodeOwnershipExtension(): CodeOwnershipExtension =
    requireNotNull(extensions.findByType()) {
        "You must apply plugin `com.avito.android.code-ownership` to the root project to run this task"
    }

internal fun CodeOwnershipExtension.requireOwnersSerializerProvider(): OwnerSerializerProvider =
    requireNotNull(ownerSerializersProvider.orNull) {
        "You must initialize ownership.ownerSerializer that can serialize/deserialize code owner!"
    }
