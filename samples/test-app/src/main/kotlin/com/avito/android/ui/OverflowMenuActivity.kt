package com.avito.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OverflowMenuActivity : AppCompatActivity() {
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overflow)
        setSupportActionBar(findViewById(R.id.toolbar))
        textView = findViewById(R.id.text)
        textView.text = ""
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val showAsAction = intent.getIntExtra(EXTRA_SHOW_AS_ACTION, INVALID_SHOW_AS_ACTION)
        val label = intent.getStringExtra(EXTRA_LABEL)
        menu.add("check").apply {
            setIcon(R.drawable.ic_check_black_24dp)
            setShowAsAction(showAsAction)
            setOnMenuItemClickListener {
                textView.text = label
                true
            }
        }
        menu.add("do not click on me").apply {
            setShowAsAction(showAsAction)
            setOnMenuItemClickListener {
                throw RuntimeException("Missed click!")
            }
        }
        return true
    }

    companion object {
        private const val INVALID_SHOW_AS_ACTION = -1
        private const val EXTRA_SHOW_AS_ACTION = "SHOW_AS_ACTION"
        private const val EXTRA_LABEL = "label"

        fun intent(showAsAction: Int, label: String): (Intent) -> Intent =
            { it.putExtra(EXTRA_SHOW_AS_ACTION, showAsAction).putExtra(EXTRA_LABEL, label) }
    }
}
