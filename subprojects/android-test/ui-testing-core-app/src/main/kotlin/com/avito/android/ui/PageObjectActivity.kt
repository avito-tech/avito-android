package com.avito.android.ui

import MinificationSample
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

class PageObjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootId = intent.getIntExtra(EXTRA_LAYOUT_ID, -1)
        check(rootId != -1) { "$EXTRA_LAYOUT_ID is mandatory" }

        setContentView(rootId)

        val sample = MinificationSample("sample usage to keep the class")
        sample.valueChanges.subscribe()
    }

    companion object {
        private const val EXTRA_LAYOUT_ID = "EXTRA_LAYOUT_ID"

        fun intent(@LayoutRes layoutId: Int): (Intent) -> Intent = {
            it.putExtra(EXTRA_LAYOUT_ID, layoutId)
        }
    }
}
