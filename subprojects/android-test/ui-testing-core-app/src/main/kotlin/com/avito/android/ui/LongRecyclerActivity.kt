package com.avito.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LongRecyclerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@LongRecyclerActivity)
            adapter = Adapter(intent.getStringArrayListExtra(EXTRA_LIST)!!)
        }
    }

    companion object {
        private const val EXTRA_LIST = "RECYCLER_LIST"

        fun intent(list: ArrayList<String>): (Intent) -> Intent =
            { it.putStringArrayListExtra(EXTRA_LIST, list) }
    }

    private class Adapter(private val hints: List<String>) :
        RecyclerView.Adapter<LabelHolder>() {

        override fun onBindViewHolder(holder: LabelHolder, position: Int) {
            holder.title.text = hints[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelHolder {
            val inflate: (layoutId: Int) -> View =
                { LayoutInflater.from(parent.context).inflate(it, parent, false) }
            return LabelHolder(inflate(R.layout.long_recycler))
        }

        override fun getItemCount() = hints.size

        override fun getItemViewType(position: Int): Int = position
    }

    private class LabelHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)
    }
}
