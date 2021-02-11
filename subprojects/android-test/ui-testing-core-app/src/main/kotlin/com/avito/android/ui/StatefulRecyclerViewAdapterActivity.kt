package com.avito.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StatefulRecyclerViewAdapterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        val changeStateForTestBindings = intent.getBooleanExtra(
            CHANGE_STATE_FOR_TEST_BINDINGS_KEY,
            false
        )

        val data = (1..99)
            .map {
                StatefulItem(
                    title = it.toString(),
                    viewedCount = 0
                )
            }
            .toList()

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(this@StatefulRecyclerViewAdapterActivity)
            adapter = StatefulAdapter(
                changeStateForTestBindings = changeStateForTestBindings,
                items = data.toMutableList()
            )
        }
    }

    private class StatefulAdapter(
        private val changeStateForTestBindings: Boolean,
        private val items: MutableList<StatefulItem>
    ) : RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val isRealHolderBinding = !holder.isFakeHolderForTests()

            if (isRealHolderBinding || changeStateForTestBindings) {
                items[position].viewedCount++
            }
            holder.title.text = items[position].title
            holder.title2.text = items[position].viewedCount.toString()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.cell_with_multiple_text_views,
                    parent,
                    false
                )
            )

        override fun getItemCount() = items.size

        /**
         * See method itemsMatching inside RecyclerViewActions.kt for understanding what happens here
         */
        @Suppress("MagicNumber")
        private fun RecyclerView.ViewHolder.isFakeHolderForTests(): Boolean =
            itemView.getTag(Integer.MAX_VALUE - 228) != null
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val title2: TextView = itemView.findViewById(R.id.title2)
    }

    private data class StatefulItem(
        val title: String,
        var viewedCount: Int = 0
    )

    companion object {
        const val CHANGE_STATE_FOR_TEST_BINDINGS_KEY = "change_state_for_test_bindings_key"
    }
}
