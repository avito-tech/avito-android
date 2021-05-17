package com.avito.android

import me.champeau.jdoctor.builders.Builder

inline fun <reified T> build(builder: Builder?): T = Builder.build(builder)
