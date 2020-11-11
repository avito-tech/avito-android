package com.avito.android.lint.html

import kotlinx.html.CommonAttributeGroupFacadeFlowSectioningContent
import kotlinx.html.HTMLTag
import kotlinx.html.SectioningOrFlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.visit

internal open class MAIN(initialAttributes: Map<String, String>, override val consumer: TagConsumer<*>) :
    HTMLTag("main", consumer, initialAttributes, null, false, false),
    CommonAttributeGroupFacadeFlowSectioningContent

internal fun SectioningOrFlowContent.main(classes: String? = null, block: MAIN.() -> Unit = {}): Unit = MAIN(
    attributesMapOf("class", classes), consumer
).visit(block)

