package me.bytebeats.views.scrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created by bytebeats on 2021/10/15 : 17:17
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class CoherentScrollingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var scrollChildRes = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    fun getDrawingPosition(child: View): Int {
        return 0
    }

    fun isStickyView(child: View): Boolean {
        return false
    }

    fun theChildIsStick(child: View): Boolean {
        return false
    }

    class LayoutParams : MarginLayoutParams {
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: LayoutParams) : super(source)
        constructor(source: ViewGroup.LayoutParams) : super(source)

        /**
         * 是否与父布局整体滑动，设置为false时，父布局不会拦截它的事件，滑动事件将由子view处理。
         * 可以实现子view内部的垂直滑动。
         */
        var isCoherent = true

        /**
         * 是否支持嵌套滑动，默认支持，如果子view或它内部的下级view实现了NestedScrollingChild接口，
         * 它可以与CoherentScrollingLayout嵌套滑动，把isNestedScroll设置为false可以禁止它与CoherentScrollingLayout嵌套滑动。
         */
        var isNestedScroll = true

        /**
         * 设置子view是否支持吸顶悬浮
         */
        var isSticky = false

        /**
         * 在View吸顶的状态下，是否可以触摸view来滑动CoherentScrollingLayout布局。
         * 默认为false，则View吸顶的状态下，不能触摸它来滑动布局
         */
        var invokeScrollWhenPinned = false

        /**
         * 吸顶下沉模式
         * 默认情况下，吸顶view在吸顶状态下，会显示在布局上层，覆盖其他布局。
         * 如果设置了下沉模式，则会相反，view在吸顶时会显示在下层，被其他布局覆盖，隐藏在下面。
         */
        var overlayWhenPinned = false
        var scrollChildRes: Int = 0
        var align = Align.START

        /**
         * child view 相对于 parent view 的位置关系
         */
        enum class Align(private val value: Int) {
            START(1), END(2), CENTER(3);

            companion object {
                fun from(value: Int): Align = values().firstOrNull { it.value == value } ?: START
            }
        }
    }
}