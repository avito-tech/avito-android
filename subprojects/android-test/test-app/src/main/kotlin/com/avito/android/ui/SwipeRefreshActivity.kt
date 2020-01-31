package com.avito.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class SwipeRefreshActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    lateinit var recycler: SwipeRefreshLayout
    var refreshedTimes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_refresh)

        recycler = findViewById(R.id.swipe_refresh)
        recycler.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        refreshedTimes += 1
    }

    fun postAndStopRefreshing() {
        recycler.handler.postDelayed({ recycler.isRefreshing = false }, 50)
    }
}
