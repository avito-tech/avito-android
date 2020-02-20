package com.avito.android.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.lang.RuntimeException

class RetryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retry)

        simulateOneShotButtonWithError()
    }

    private var buttonClicksCounter = 0

    private fun simulateOneShotButtonWithError() {
        val button = findViewById<View>(R.id.button)
        val buttonClickIndicator = findViewById<View>(
            R.id.button_click_indicator
        )

        button.setOnClickListener {
            button.visibility = View.GONE
            buttonClickIndicator.visibility = View.VISIBLE

            buttonClicksCounter++
            if (buttonClicksCounter == 1) {
                throw UnexpectedFatalError()
            }
        }
    }
}

class UnexpectedFatalError : RuntimeException()
