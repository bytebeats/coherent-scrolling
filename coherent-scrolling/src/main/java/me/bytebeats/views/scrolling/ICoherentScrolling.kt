package me.bytebeats.views.scrolling

import android.view.View

/**
 * Created by bytebeats on 2021/10/15 : 16:57
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * CoherentScrollingLayout only scrolls with its direct child views by default.
 * For that CoherentScrollingLayout can scroll with child views of its direct child views, ICoherentScrolling is needed.
 * Child views which implement ICoherentScrolling tells CoherentScrollingLayout which view would scroll with,
 * then CoherentScrollingLayout can scroll with right child view.
 */
interface ICoherentScrolling {
    /**
     * current child view which would scroll with CoherentScrollingLayout.
     * Only one scrollable child view when CoherentScrollingLayout is scrolling with
     */
    fun currentScrollableView(): View

    /**
     * All child views which is able to scroll with CoherentScrollingLayout.
     */
    fun scrollableViews(): List<View>
}