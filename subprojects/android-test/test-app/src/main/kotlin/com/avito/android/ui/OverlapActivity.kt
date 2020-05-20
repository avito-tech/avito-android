package com.avito.android.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class OverlapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlap)

        val rootView = findViewById<View>(R.id.root)

        findViewById<Button>(R.id.snack_button).setOnClickListener {
            Snackbar.make(rootView, "Hello", Snackbar.LENGTH_LONG).show()

        }
    }

}