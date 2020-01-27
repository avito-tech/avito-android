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

class IdenticalCellsRecyclerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@IdenticalCellsRecyclerActivity)
            adapter = Adapter(intent.getStringArrayListExtra(EXTRA_LIST))
        }
    }

    companion object {
        private const val EXTRA_LIST = "RECYCLER_LIST"

        fun intent(list: ArrayList<String>): (Intent) -> Intent =
            { it.putStringArrayListExtra(EXTRA_LIST, list) }
    }

    private class Adapter(private val items: List<String>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = items[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.cell, parent, false)
            )

        override fun getItemCount() = items.size
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)
    }
}
