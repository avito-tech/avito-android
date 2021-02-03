package com.avito.android.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TextElementActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var textViewLong: TextView

    var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_elements)
        textView = findViewById(R.id.text_view)
        textViewLong = findViewById(R.id.text_view_long)
        val text = "Text with clickable link"
        val spannableString = SpannableString(text)
        spannableString.makeSpannable(text, "link")
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()

        val longText =
            "Long text containing multiple links: link 1, link 2 and even long link which can have\nmultiple lines"
        val longSpannable = SpannableString(longText)
        val longTextPart = "long link which can have\nmultiple lines"
        longSpannable.makeSpannable(longText, "link 1")
        longSpannable.makeSpannable(longText, "link 2")
        longSpannable.makeSpannable(longText, longTextPart)
        textViewLong.text = longSpannable
        textViewLong.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun SpannableString.makeSpannable(text: String, textToSpan: String) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                count++
            }
        }
        setSpan(
            clickableSpan,
            text.indexOf(textToSpan),
            text.indexOf(textToSpan) + textToSpan.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
