package me.bytebeats.views.scrolling

import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by bytebeats on 2021/10/15 : 17:28
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

fun ViewGroup.MarginLayoutParams?.invalidateVerticalMargins() {
    this?.topMargin = 0
    this?.bottomMargin = 0
}

internal fun computeVerticalScrollOffset(view: View): Int {
    val scrollableView = scrollableView(view)
    if (scrollableView is ScrollingView) {
        return scrollableView.computeVerticalScrollOffset()
    }
    try {
        val method = View::class.java.getDeclaredMethod("computeVerticalScrollOffset")
        method.isAccessible = true
        val result = method.invoke(scrollableView)
        if (result != null) {
            return result as Int
        }
    } catch (ignored: Exception) {
    }
    return scrollableView.scrollY
}

internal fun computeVerticalScrollRange(view: View): Int {
    val scrollableView = scrollableView(view)
    if (scrollableView is ScrollingView) {
        return scrollableView.computeVerticalScrollRange()
    }
    try {
        val method = View::class.java.getDeclaredMethod("computeVerticalScrollRange")
        method.isAccessible = true
        val result = method.invoke(scrollableView)
        if (result != null) {
            return result as Int
        }
    } catch (ignored: Exception) {
    }
    return scrollableView.height
}

internal fun computeVerticalScrollExtent(view: View): Int {
    val scrollableView = scrollableView(view)
    if (scrollableView is ScrollingView) {
        return scrollableView.computeVerticalScrollExtent()
    }
    try {
        val method = View::class.java.getDeclaredMethod("computeVerticalScrollExtent")
        method.isAccessible = true
        val result = method.invoke(scrollableView)
        if (result != null) {
            return result as Int
        }
    } catch (ignored: Exception) {
    }
    return scrollableView.height
}

/**
 * ??????View?????????????????????????????????
 */
internal fun offsetOfScrollToTop(view: View): Int =
    if (isCoherentScrollingChildView(view) && canScrollVertically(view))
        (-computeVerticalScrollOffset(view)).coerceAtMost(-1)
    else 0

/**
 * ??????View?????????????????????????????????
 */
internal fun offsetOfScrollToBottom(view: View): Int =
    if (isCoherentScrollingChildView(view) && canScrollVertically(view))
        (computeVerticalScrollRange(view) - computeVerticalScrollOffset(view) - computeVerticalScrollExtent(
            view
        )).coerceAtLeast(1)
    else 0

internal val mBounds = Rect()

internal fun canScrollVertically(view: View, direction: Int): Boolean {
    val scrollableView = scrollableView(view)
    if (!scrollableView.isVisible) {
        return false
    }
    if (scrollableView is AbsListView) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) scrollableView.canScrollList(
            direction
        ) else false //???????????????(android 19??????)???AbsListView???????????????
    } else {
        // RecyclerView??????canScrollVertically?????????????????????????????????????????????????????????
        if (scrollableView is RecyclerView) {
            // ??????recyclerView?????????????????????????????????canScrollVertically???????????????????????????????????????????????????????????????
            // ?????????????????????recyclerView???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (canScrollHorizontally(scrollableView)) {
                if (!scrollableView.canScrollVertically(direction)) {
                    return false
                }
            }
            val layoutManager = scrollableView.layoutManager
            val adapter = scrollableView.adapter
            if (layoutManager != null && adapter != null && adapter.itemCount > 0) {
                layoutManager.findViewByPosition(if (direction > 0) adapter.itemCount - 1 else 0)
                    ?: return true
            } else return false

            val count = adapter.itemCount
            if (direction > 0) {
                for (i in count - 1..0 step -1) {
                    val child = scrollableView.getChildAt(i)
                    scrollableView.getDecoratedBoundsWithMargins(child, mBounds)
                    if (mBounds.bottom > scrollableView.height - scrollableView.paddingBottom) {
                        return true
                    }
                }
                return false
            } else {
                for (i in 0 until count) {
                    val child = scrollableView.getChildAt(i)
                    scrollableView.getDecoratedBoundsWithMargins(child, mBounds)
                    if (mBounds.top < scrollableView.paddingTop) {
                        return true
                    }
                }
            }
        }
        return scrollableView.canScrollVertically(direction)
    }
}

/**
 * ???????????????????????????View
 * find views under touch event
 */

internal fun touchedChildrenViews(view: View, touchX: Int, touchY: Int): List<View> {
    val views = mutableListOf<View>()
    findTouchedViews(view, touchX, touchY, views)
    return views
}

private fun findTouchedViews(view: View, touchX: Int, touchY: Int, result: MutableList<View>) {
    if (isViewInTouch(view, touchX, touchY)) {
        result.add(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTouchedViews(view[i], touchX, touchY, result)
            }
        }
    }
}

internal fun rawX(view: View, ev: MotionEvent, pointerIdx: Int): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ev.getRawX(pointerIdx).toInt()
    } else {
        val locations = IntArray(2) { 0 }
        view.getLocationOnScreen(locations)
        return (locations[0] + ev.getX(pointerIdx)).toInt()
    }
}

internal fun rawY(view: View, ev: MotionEvent, pointerIdx: Int): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ev.getRawY(pointerIdx).toInt()
    } else {
        val locations = IntArray(2) { 0 }
        view.getLocationOnScreen(locations)
        return (locations[1] + ev.getY(pointerIdx)).toInt()
    }
}

internal fun computeVerticalScrollOffsetsFor(views: List<View>): List<Int> =
    views.map { computeVerticalScrollOffset(it) }

internal infix fun <T> Collection<T>.deepEqualTo(other: Collection<T>): Boolean {
    if (this != other) {
        if (this.size != other.size) return false
        return this.zip(other).map { (a, b) -> a == b }.all { true }
    }
    return true
}

internal fun equalsOffsets(offsets1: List<Int>, offset2: List<Int>): Boolean =
    offsets1.deepEqualTo(offset2)

/**
 * ????????????????????????View???
 */
private fun isViewInTouch(view: View?, x: Int, y: Int): Boolean {
    if (view == null) return false
    val locations = IntArray(2) { 0 }
    view.getLocationOnScreen(locations)
    val left = locations[0]
    val top = locations[1]
    val right = left + view.measuredWidth
    val bottom = top + view.measuredHeight
    return x in left..right && y in top..bottom
}

/**
 * coherent scroll child view. if nothing found, return view itself
 */
private fun scrollableView(view: View): View {
    var coherentView: View?
    //child view from CoherentScrollingLayout.LayoutParams#scrollChildRes is important
    var scrollableChildView = scrollableChildView(view)
    while (scrollableChildView is ICoherentScrolling) {
        coherentView = scrollableChildView
        scrollableChildView = scrollableChildView.currentScrollableView()
        if (coherentView == scrollableChildView) {
            break
        }
    }
    return scrollableChildView
}

/**
 * find scrollable child view through CoherentScrollingLayout.LayoutParams#scrollChildRes.
 * If nothing found, return view itself
 */
private fun scrollableChildView(view: View): View {
    val lp = view.layoutParams
    if (lp is CoherentScrollingLayout.LayoutParams) {
        val childViewRes = lp.scrollChildRes
        if (childViewRes != View.NO_ID) {
            val child = view.findViewById<View>(childViewRes)
            if (child != null) {
                return child
            }
        }
    }
    return view
}

/**
 * ??????View??????????????????????????????
 */
private fun isCoherentScrollingChildView(view: View?): Boolean = view?.let {
    val lp = it.layoutParams
    if (lp is CoherentScrollingLayout.LayoutParams) {
        lp.isCoherent
    } else true
} ?: false

/**
 * View??????????????????????????????(??????????????????????????????????????????????????????)
 */
internal fun canScrollHorizontally(view: View): Boolean =
    view.canScrollHorizontally(1) || view.canScrollHorizontally(-1)

/**
 * View??????????????????????????????(??????????????????????????????????????????????????????)
 */
internal fun canScrollVertically(view: View): Boolean =
    view.canScrollVertically(1) || view.canScrollVertically(-1)

internal fun startInterceptRequestLayout(recyclerView: RecyclerView): Boolean {
    if ("InterceptRequestLayout" == recyclerView.tag) {
        try {
            val method = RecyclerView::class.java.getDeclaredMethod("startInterceptRequestLayout")
            method.isAccessible = true
            method.invoke(recyclerView)
            return true
        } catch (ignored: Exception) {

        }
    }
    return false
}

internal fun stopInterceptRequestLayout(recyclerView: RecyclerView) {
    if ("InterceptRequestLayout" == recyclerView.tag) {
        try {
            val method = RecyclerView::class.java.getDeclaredMethod(
                "stopInterceptRequestLayout",
                Boolean::class.java
            )
            method.isAccessible = true
            method.invoke(recyclerView, false)
        } catch (ignored: Exception) {

        }
    }
}

/**
 * ??????????????????????????? isCoherent???true.??????????????????CoherentScrollingLayout??????
 */
internal fun isCoherentScrollingParentView(view: View): Boolean {
    var child = view
    while (child.parent is ViewGroup && child.parent !is CoherentScrollingLayout) {
        child = child.parent as View
    }
    return if (child.parent is CoherentScrollingLayout) isCoherentScrollingChildView(child) else false
}

/**
 * ?????????????????????????????????view??????????????????
 */
internal fun hasHorizontalScrollChildViewsUnderTouch(
    view: View?,
    touchX: Int,
    touchY: Int
): Boolean {
    return if (view == null) false
    else touchedChildrenViews(
        view,
        touchX,
        touchY
    ).any { it.canScrollHorizontally(1) || it.canScrollHorizontally(-1) }
}

private fun topViewInTouch(
    coherentScrollingLayout: CoherentScrollingLayout,
    touchX: Int,
    touchY: Int
): View? {
    var topTouchView: View? = null
    for (i in 0 until coherentScrollingLayout.childCount) {
        val child = coherentScrollingLayout[i]
        if (child.isVisible && isViewInTouch(child, touchX, touchY)) {
            if (topTouchView == null) {
                topTouchView = child
                continue
            }
            if (ViewCompat.getZ(child) > ViewCompat.getZ(topTouchView)
                || (ViewCompat.getZ(child) == ViewCompat.getZ(topTouchView)
                        && coherentScrollingLayout.getDrawingPosition(child) > coherentScrollingLayout.getDrawingPosition(
                    topTouchView
                ))
            ) {
                topTouchView = child
            }
        }
    }
    return topTouchView
}

/**
 * ???????????????????????????CoherentScrollingLayout
 */
internal fun coherentScrollingLayoutsInTouch(
    view: View,
    touchX: Int,
    touchY: Int
): List<CoherentScrollingLayout> {
    return touchedChildrenViews(view, touchX, touchY).filterIsInstance<CoherentScrollingLayout>()
}

/**
 * ??????????????????view??????????????????????????????
 */
internal fun isDisableScrollWhenPinnedSticky(view: View, touchX: Int, touchY: Int): Boolean {
    val coherentScrollingLayouts = coherentScrollingLayoutsInTouch(view, touchX, touchY)
    for (layout in coherentScrollingLayouts.reversed()) {
        val topView = topViewInTouch(layout, touchX, touchY)
        if (topView != null && layout.isStickyView(topView) && layout.theChildIsStick(topView)) {
            val lp = topView.layoutParams as CoherentScrollingLayout.LayoutParams
            if (!lp.invokeScrollWhenPinned) {
                return true
            }
        }
    }
    return false
}