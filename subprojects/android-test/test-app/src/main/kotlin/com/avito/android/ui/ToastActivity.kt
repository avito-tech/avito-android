package com.avito.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.android.util.showToast

class ToastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)

        showToast("I'am a toast!")
    }
}
