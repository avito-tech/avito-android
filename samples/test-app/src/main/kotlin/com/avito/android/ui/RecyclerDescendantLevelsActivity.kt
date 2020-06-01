package com.avito.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerDescendantLevelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@RecyclerDescendantLevelsActivity)
            adapter = Adapter()
        }
    }

    private class Adapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_find_descendant, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun getItemCount(): Int = 1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // empty
        }
    }
}
