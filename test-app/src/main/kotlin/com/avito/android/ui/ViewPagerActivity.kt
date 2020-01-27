package com.avito.android.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class ViewPagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)

        val pager: ViewPager = findViewById(R.id.view_pager)
        val adapter = EvenOddPageAdapter(100, this)
        pager.adapter = adapter
    }

    private class EvenOddPageAdapter(
        private val count: Int,
        private val context: Context
    ) : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(context)

            val layoutId = when (parityOfNumber(position)) {
                Parity.EVEN -> R.layout.view_pager_even_item
                Parity.ODD -> R.layout.view_pager_odd_item
            }

            val layout = inflater.inflate(layoutId, collection, false)

            collection.addView(layout)

            return layout
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            view as View
            collection.removeView(view)
        }

        override fun getCount(): Int = count

        override fun isViewFromObject(view: View, value: Any): Boolean = view == value

        override fun getPageTitle(position: Int): CharSequence = when (parityOfNumber(position)) {
            Parity.EVEN -> "Even"
            Parity.ODD -> "Odd"
        }

        private fun parityOfNumber(number: Int): Parity = when (number % 2) {
            0 -> Parity.EVEN
            else -> Parity.ODD
        }

        private enum class Parity {
            EVEN,
            ODD
        }
    }
}
