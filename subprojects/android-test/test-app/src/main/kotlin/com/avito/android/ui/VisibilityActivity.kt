package com.avito.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class VisibilityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visibility)

        SynthUser().dostuff()
    }
}
