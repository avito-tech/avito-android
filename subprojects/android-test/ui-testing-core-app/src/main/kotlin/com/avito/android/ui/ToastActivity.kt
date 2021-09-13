package com.avito.android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avito.android.util.ProxyToast
import com.avito.android.util.showToast

class ToastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)

        findViewById<View>(R.id.root).post {
            setupSimpleToast()
            setupCustomToast()
        }
    }

    private fun setupCustomToast() {
        if (intent.getBooleanExtra(EXTRA_SHOW_CUSTOM_TOAST, false)) {
            val layout: View = layoutInflater.inflate(
                R.layout.custom_toast,
                findViewById<View>(R.id.root) as ViewGroup,
                false
            )
            @SuppressLint("SetTextI18n")
            layout.findViewById<TextView>(R.id.text).text = "Custom toast"

            val toast = Toast(applicationContext)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            toast.duration = Toast.LENGTH_LONG
            @Suppress("DEPRECATION")
            toast.view = layout

            ProxyToast.instance.show(toast)
        }
    }

    private fun setupSimpleToast() {
        if (intent.getBooleanExtra(EXTRA_SHOW_SIMPLE_TOAST, false)) {
            showToast("Simple toast")
        }
    }

    companion object {

        private const val EXTRA_SHOW_SIMPLE_TOAST = "extra_simple_toast"
        private const val EXTRA_SHOW_CUSTOM_TOAST = "extra_custom_toast"

        fun intent(
            showSimpleToast: Boolean = false,
            showCustomToast: Boolean = false,
        ): (Intent) -> Intent = {
            it.putExtra(EXTRA_SHOW_SIMPLE_TOAST, showSimpleToast)
            it.putExtra(EXTRA_SHOW_CUSTOM_TOAST, showCustomToast)
        }
    }
}
