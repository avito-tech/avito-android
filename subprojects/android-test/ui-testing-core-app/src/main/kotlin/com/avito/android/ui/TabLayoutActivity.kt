package com.avito.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class TabLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_layout)

        findViewById<TabLayout>(R.id.tabs).apply {
            tabMode = TabLayout.MODE_SCROLLABLE
            @Suppress("MagicNumber")
            for (i in 1..1000) {
                addTab(newTab().setText("Tab $i"))
            }
        }
    }
}
