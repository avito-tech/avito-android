package com.avito.android.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RotateOrientationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotate)

        setupOrientationLabel()
        setupDialog()
        setupPopup()
    }

    private fun setupOrientationLabel() {
        val orientation = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_UNDEFINED -> "UNDEFINED"
            Configuration.ORIENTATION_LANDSCAPE -> "LANDSCAPE"
            Configuration.ORIENTATION_PORTRAIT -> "PORTRAIT"
            Configuration.ORIENTATION_SQUARE -> "SQUARE"
            else -> "UNKNOWN"
        }
        findViewById<TextView>(R.id.orientation_label).text = orientation
    }

    @SuppressLint("SetTextI18n")
    private fun setupDialog() {
        if (intent.getBooleanExtra(EXTRA_OPEN_DIALOG, false)) {
            AlertDialog.Builder(this)
                .setMessage("Alert dialog")
                .setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int ->
                    dialog?.cancel()
                }
                .setPositiveButton("Ok") { dialog: DialogInterface?, _: Int ->
                    dialog?.cancel()
                }
                .show()
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun setupPopup() {
        if (intent.getBooleanExtra(EXTRA_OPEN_POPUP, false)) {
            val content = findViewById<View>(R.id.content)

            val view = LayoutInflater.from(this).inflate(R.layout.popup_window, null)

            val popup = PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popup.setBackgroundDrawable(ColorDrawable(Color.GREEN))
            view.findViewById<TextView>(R.id.title).text = "Popup Window content"

            content.post {
                popup.showAtLocation(content, Gravity.CENTER, 0, 0)
            }
        }
    }

    companion object {

        private const val EXTRA_OPEN_DIALOG = "extra_open_dialog"
        private const val EXTRA_OPEN_POPUP = "extra_open_popup"

        fun intent(
            openDialog: Boolean = false,
            openPopup: Boolean = false,
        ): (Intent) -> Intent = {
            it.putExtra(EXTRA_OPEN_DIALOG, openDialog)
            it.putExtra(EXTRA_OPEN_POPUP, openPopup)
        }
    }
}
