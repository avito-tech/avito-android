package com.avito.android.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class RetryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retry)

        setupButton()
    }

    private fun setupButton() {
        val button = findViewById<View>(R.id.button)
        val buttonClickIndicator = findViewById<View>(R.id.button_click_indicator)

        button.setOnClickListener {
            button.visibility = View.GONE
            buttonClickIndicator.visibility = View.VISIBLE
        }
    }
}
