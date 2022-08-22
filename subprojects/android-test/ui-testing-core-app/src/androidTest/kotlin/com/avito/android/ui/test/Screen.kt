package com.avito.android.ui.test

import com.avito.android.test.page_object.Alert
import com.avito.android.ui.test.dialog.DialogsScreen
import com.avito.android.ui.test.retry.RetryScreen

object Screen {

    val distantViewOnScroll: DistantViewOnScrollScreen
        get() = DistantViewOnScrollScreen()

    val visibility: VisibilityScreen
        get() = VisibilityScreen()

    val overflow: OverflowMenuScreen
        get() = OverflowMenuScreen()

    val swipeRefresh: SwipeRefreshScreen
        get() = SwipeRefreshScreen()

    val textsElements: TextElementsScreen
        get() = TextElementsScreen()

    val retry: RetryScreen
        get() = RetryScreen()

    val buttons: ButtonsScreen
        get() = ButtonsScreen()

    val movingButton: MovingButtonScreen
        get() = MovingButtonScreen()

    val snackbarScreen: SnackbarScreen
        get() = SnackbarScreen()

    val identicalCellsRecycler: IdenticalCellsRecyclerScreen
        get() = IdenticalCellsRecyclerScreen()

    val buttonsOverRecycler: ButtonsOverRecyclerScreen
        get() = ButtonsOverRecyclerScreen()

    val statefulRecyclerViewAdapterScreen: StatefulRecyclerViewAdapterScreen
        get() = StatefulRecyclerViewAdapterScreen()

    val recyclerAsLayout: RecyclerAsLayoutScreen
        get() = RecyclerAsLayoutScreen()

    val viewPagerScreen: ViewPagerScreen
        get() = ViewPagerScreen()

    val longRecycler: LongRecyclerScreen
        get() = LongRecyclerScreen()

    val recyclerInRecycler: RecyclerInRecyclerLayoutScreen
        get() = RecyclerInRecyclerLayoutScreen()

    val editTextScreen: EditTextScreen
        get() = EditTextScreen()

    val tabLayoutScreen: TabLayoutScreen
        get() = TabLayoutScreen()

    val drawablesScreen: DrawablesScreen
        get() = DrawablesScreen()

    val bitmapScreen: BitmapScreen
        get() = BitmapScreen()

    val recyclerWithSingleLongItemScreen: RecyclerWithSingleLongItemScreen
        get() = RecyclerWithSingleLongItemScreen()

    val recyclerDescendantLevelsScreen: RecyclerDescendantLevelsScreen
        get() = RecyclerDescendantLevelsScreen()

    val overlapScreen: OverlapScreen
        get() = OverlapScreen()

    val dialogsScreen: DialogsScreen
        get() = DialogsScreen()

    val alert: Alert
        get() = Alert()
}
