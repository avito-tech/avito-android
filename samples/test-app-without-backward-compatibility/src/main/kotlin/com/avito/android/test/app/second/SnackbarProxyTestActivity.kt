package com.avito.android.test.app.second

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.avito.android.snackbar.proxy.showSnackbar
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.atomic.AtomicInteger

class SnackbarProxyTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snackbar_proxy_test)
        val number = AtomicInteger(1)
        findViewById<Button>(R.id.show_snackbar_short).setOnClickListener {
            Snackbar.make(
                findViewById(android.R.id.content),
                "snackbar number ${number.getAndIncrement()}",
                Snackbar.LENGTH_SHORT
            ).showSnackbar()
        }
        val handler = Handler()
        val delayMs: Long = 1000
        findViewById<Button>(R.id.show_snackbar_delayed).setOnClickListener {
            handler.postDelayed({
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "snackbar number ${number.getAndIncrement()}",
                    Snackbar.LENGTH_SHORT
                ).showSnackbar()
            }, delayMs)
        }
    }
}
