package com.avito.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ButtonsOverRecyclerWithCollapsingToolbarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buttons_over_recycler_with_collapsing_toolbar)

        val data = (1..99).map { it.toString() }.toList()

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager =
                LinearLayoutManager(this@ButtonsOverRecyclerWithCollapsingToolbarActivity)
            adapter = Adapter(data)
        }

        findViewById<Button>(R.id.bottom_button).setOnClickListener {
            throw RuntimeException("Bottom button clicked!")
        }
    }

    private class Adapter(private val items: List<String>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = items[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell, parent, false))

        override fun getItemCount() = items.size
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)
    }
}
