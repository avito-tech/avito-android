package com.avito.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView

class RecyclerInRecyclerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@RecyclerInRecyclerActivity)
            adapter = Adapter(intent.getStringArrayListExtra(EXTRA_LIST))
        }
    }

    companion object {
        private const val EXTRA_LIST = "RECYCLER_LIST"

        fun intent(list: ArrayList<String>): (Intent) -> Intent =
            { it.putStringArrayListExtra(EXTRA_LIST, list) }
    }

    private class Adapter(private val innerRecyclerItems: List<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when (holder) {
                is RecyclerHolder -> with(holder.recycler) {
                    layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
                    adapter = InnerAdapter(innerRecyclerItems)
                }
                else -> error("Unsupported holder type: ${holder.javaClass.name}")
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cell_with_inner_recycler, parent, false)

            return RecyclerHolder(view)
        }

        override fun getItemCount() = 1

        override fun getItemViewType(position: Int): Int = 0
    }

    private class InnerAdapter(private val labels: List<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when (holder) {
                is LabelHolder -> holder.title.text = labels[position]
                else -> error("Unsupported holder type: ${holder.javaClass.name}")
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cell, parent, false)

            return LabelHolder(view)
        }

        override fun getItemCount() = labels.size

        override fun getItemViewType(position: Int): Int = 0
    }

    private class LabelHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)
    }

    private class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val recycler = itemView as RecyclerView
    }
}
