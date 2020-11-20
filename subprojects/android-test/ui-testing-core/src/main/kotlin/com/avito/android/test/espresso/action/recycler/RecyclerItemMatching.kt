package com.avito.android.test.espresso.action.recycler

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.intent.Checks.checkArgument
import androidx.test.espresso.util.HumanReadables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import kotlin.math.max
import kotlin.math.min

private const val FAKE_RENDERING_VIEW_HOLDER_TAG_KEY = Integer.MAX_VALUE - 228
private const val FAKE_RENDERING_VIEW_HOLDER_TAG_VALUE = "RENDERED_FOR_FAKE_RUN"
private const val NEAR_ITEMS_SEARCH_WINDOW_SIZE = 5

internal class RecyclerItemsMatcher(
    private val recyclerView: RecyclerView
) {

    internal sealed class Match<VH : RecyclerView.ViewHolder> {

        abstract val matcher: TypeSafeMatcher<VH>

        data class AtPosition<VH : RecyclerView.ViewHolder>(
            val position: Int,
            override val matcher: TypeSafeMatcher<VH>
        ) : Match<VH>() {
            init {
                checkArgument(position >= 0, "%d is used as an index - must be >= 0", position)
            }
        }

        data class All<VH : RecyclerView.ViewHolder>(
            override val matcher: TypeSafeMatcher<VH>
        ) : Match<VH>()

        companion object {
            fun <VH : RecyclerView.ViewHolder> create(
                position: Int?,
                matcher: TypeSafeMatcher<VH>
            ): Match<VH> {
                return when (position) {
                    null -> All(matcher)
                    else -> AtPosition(position, matcher)
                }
            }
        }
    }

    internal sealed class Result {

        abstract val description: String

        /**
         * When item found
         */
        data class Found(
            val item: MatchedItem
        ) : Result() {
            override val description: String
                get() = "Found item at position:${item.position}"
        }

        /**
         * When we looked all items and there wasn't match
         */
        data class NoItem(
            val size: Int
        ) : Result() {
            override val description: String
                get() = "No item in recycler. Recycler size: $size"
        }

        /**
         * When position for matching is greater then [RecyclerView.Adapter.getItemCount]
         */
        data class IndexOutOfBound(
            val atPosition: Int,
            val size: Int
        ) : Result() {
            override val description: String
                get() = "Tried to match item at position $atPosition. But recycler size is $size"
        }

        /**
         * When item at position doesn't match
         */
        data class NoItemAtPosition(
            val atPosition: Int,
            val itemAtPosition: String,
            val nearMatchedItems: List<MatchedItem>,
            val searchFrom: Int,
            val searchTo: Int
        ) : Result() {
            override val description: String
                get() = "No matched item in recycler at position $atPosition. ${createNearItemsDescription()}"

            private fun createNearItemsDescription() =
                "Search near items from $searchFrom to $searchTo ${
                    if (nearMatchedItems.isEmpty()) "doesn't have matches" else "has matches at positions: [${
                        nearMatchedItems.map { it.position }
                            .joinToString()
                    }"
                }]"
        }
    }

    private val adapter = requireNotNull(recyclerView.adapter)
    private val itemCount = adapter.itemCount
    private val viewHolderCache = SparseArray<RecyclerView.ViewHolder>(itemCount)

    fun <VH : RecyclerView.ViewHolder> match(
        match: Match<VH>
    ): Result {
        return when (match) {
            is Match.AtPosition -> matchAtPosition(match.matcher, match.position)
            is Match.All -> matchAll(match.matcher)
        }
    }

    private fun <VH : RecyclerView.ViewHolder> matchAll(viewHolderMatcher: TypeSafeMatcher<VH>): Result {
        val (position, holder) = (0 until itemCount)
            .asSequence()
            .map { position -> position to getViewHolder(position) }
            .firstOrNull { (_, holder) ->
                viewHolderMatcher.matches(holder)
            } ?: return Result.NoItem(size = itemCount)

        return Result.Found(
            item = MatchedItem(
                position = position,
                description = HumanReadables.getViewHierarchyErrorMessage(
                    holder.itemView,
                    null,
                    "\n\n*** Matched ViewHolder item at position: $position ***",
                    null
                )
            )
        )
    }

    private fun <VH : RecyclerView.ViewHolder> matchAtPosition(
        viewHolderMatcher: TypeSafeMatcher<VH>,
        atPosition: Int
    ): Result {
        return when {
            atPosition > itemCount - 1 -> Result.IndexOutOfBound(
                atPosition = atPosition,
                size = itemCount
            )
            else -> {
                val viewHolderAtPosition = getViewHolder(atPosition)

                if (viewHolderMatcher.matches(viewHolderAtPosition)) {
                    Result.Found(
                        item = MatchedItem(
                            position = atPosition,
                            description = HumanReadables.getViewHierarchyErrorMessage(
                                viewHolderAtPosition.itemView,
                                null,
                                "\n\n*** Matched ViewHolder item at position: $atPosition ***",
                                null
                            )
                        )
                    )
                } else {
                    val searchFrom = max(0, atPosition - NEAR_ITEMS_SEARCH_WINDOW_SIZE)
                    val searchTo = min(itemCount, atPosition + NEAR_ITEMS_SEARCH_WINDOW_SIZE)
                    val nearMatchedItems = (searchFrom until searchTo)
                        .asSequence()
                        .map { position ->
                            position to getViewHolder(position)
                        }
                        .filter { (_, holder) ->
                            viewHolderMatcher.matches(holder)
                        }
                        .map { (position, holder) ->
                            MatchedItem(
                                position = position,
                                description = HumanReadables.getViewHierarchyErrorMessage(
                                    holder.itemView,
                                    null,
                                    "\n\n*** Matched ViewHolder item at position: $atPosition ***",
                                    null
                                )
                            )
                        }
                        .toList()

                    Result.NoItemAtPosition(
                        atPosition = atPosition,
                        itemAtPosition = HumanReadables.getViewHierarchyErrorMessage(
                            viewHolderAtPosition.itemView,
                            null,
                            "\n\n*** Matched ViewHolder item at position: $atPosition ***",
                            null
                        ),
                        nearMatchedItems = nearMatchedItems,
                        searchFrom = searchFrom,
                        searchTo = searchTo
                    )
                }
            }
        }
    }

    private fun getViewHolder(atPosition: Int): RecyclerView.ViewHolder {
        var viewHolder = recyclerView.findViewHolderForAdapterPosition(atPosition)
        if (viewHolder == null) {
            val itemType = adapter.getItemViewType(atPosition)

            viewHolder = viewHolderCache.get(itemType)

            if (viewHolder == null) {

                viewHolder = adapter.createViewHolder(recyclerView, itemType)

                /**
                 * It allows production code to understand, that bindViewHolder has called by
                 * fake rendering process (for find element in recycler view before it will be shown on
                 * real user screen).
                 */
                viewHolder.itemView.setTag(
                    FAKE_RENDERING_VIEW_HOLDER_TAG_KEY,
                    FAKE_RENDERING_VIEW_HOLDER_TAG_VALUE
                )

                viewHolderCache.put(itemType, viewHolder)
            }
            // Bind data to ViewHolder and apply matcher to view descendants.
            adapter.bindViewHolder(viewHolder, atPosition)
        }
        return viewHolder
    }
}

fun <VH : RecyclerView.ViewHolder> viewHolderMatcher(itemViewMatcher: Matcher<View>) =
    object : TypeSafeMatcher<VH>() {
        override fun matchesSafely(viewHolder: VH): Boolean {
            return itemViewMatcher.matches(viewHolder.itemView)
        }

        override fun describeTo(description: Description) {
            description.appendText("holder with view: ")
            itemViewMatcher.describeTo(description)
        }
    }

internal class MatchedItem(
    val position: Int,
    val description: String
) {
    override fun toString(): String = description
}
