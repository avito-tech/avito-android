package com.avito.android.test.app.second

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout

class AppBarActivity : AppCompatActivity() {

    private lateinit var appBarLayout: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_bar)
        appBarLayout = findViewById(R.id.appbar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    fun setExpanded(isExpanded: Boolean) {
        appBarLayout.handler.post {
            appBarLayout.setExpanded(isExpanded)
        }
    }
}
