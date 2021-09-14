package com.avito.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * This activity tries to emulate non-list layouts rendered via RecyclerView
 */
class RecyclerAsLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@RecyclerAsLayoutActivity)
            adapter = Adapter(intent.getStringArrayListExtra(EXTRA_LIST)!!)
        }
    }

    companion object {
        private const val EXTRA_LIST = "RECYCLER_LIST"

        fun intent(list: ArrayList<String>): (Intent) -> Intent =
            { it.putStringArrayListExtra(EXTRA_LIST, list) }
    }

    private class Adapter(private val hints: List<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val hint = "${hints[position]}$position"
            when (holder) {
                is InputHolder -> holder.layout.hint = hint
                is EditHolder -> holder.editText.hint = hint
                is LabelHolder -> holder.title.text = hint
                else -> error("Unsupported holder type: ${holder.javaClass.name}")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflate: (layoutId: Int) -> View =
                { LayoutInflater.from(parent.context).inflate(it, parent, false) }

            return when (viewType) {
                ViewType.INPUT.ordinal -> InputHolder(inflate(R.layout.cell_with_text_input))
                ViewType.EDIT.ordinal -> EditHolder(inflate(R.layout.cell_with_edit_text))
                ViewType.LABEL.ordinal -> LabelHolder(inflate(R.layout.cell))
                else -> error("Unsupported viewType: $viewType")
            }
        }

        override fun getItemCount() = hints.size

        override fun getItemViewType(position: Int): Int = ViewType.valueOf(hints[position].uppercase()).ordinal
    }

    enum class ViewType { INPUT, EDIT, LABEL }

    private class InputHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val layout: TextInputLayout = itemView.findViewById(R.id.input_layout)

        val editText: TextInputEditText = itemView.findViewById(R.id.input)
    }

    private class EditHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val editText: EditText = itemView.findViewById(R.id.edit_text)
    }

    private class LabelHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)
    }
}
