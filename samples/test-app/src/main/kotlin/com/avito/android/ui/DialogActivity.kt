package com.avito.android.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DialogActivity : AppCompatActivity() {

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialog = AlertDialog.Builder(this)
            .setMessage("This is message in my AlertDialog. Extremely useful text here")
            .setNegativeButton("Negative") { _, _ -> }
            .setPositiveButton("Positive") { _, _ -> }
            .setView(LayoutInflater.from(this@DialogActivity).inflate(R.layout.dialog_view, null))
            .create()
        dialog.apply{
            setCanceledOnTouchOutside(true)
            setOnCancelListener { finish() }
            show()
        }
    }
}
