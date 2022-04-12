package com.banzhi.repeat.widget

import android.graphics.PointF
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import kotlin.math.abs

/**
 *<pre>
 * @author : jiang
 * @time : 2020/12/30.
 * @desciption : 参考  https://mp.weixin.qq.com/s/sQhqvU22LdXibC8plNolwA
 *                    https://mp.weixin.qq.com/s/DpV6iD4rBSgQQHsPNDJe6w
 * @version :
 *</pre>
 */
class RepeatLayoutManager(@RecyclerView.Orientation orientation: Int) :
    RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {


    private var mPendingPosition = RecyclerView.NO_POSITION
    private var mCurrentPosition = 0
    private lateinit var mOrientationHelper: OrientationHelper
    private var mOrientation = HORIZONTAL
    private var mFillAnchor = 0
    private var canLoop = false

    //记录当次滚动的距离
    private var mLastScrollDelta: Int = 0

    //记录软键盘弹起、收回时的滚动距离
    private var mFixOffset: Int = 0

    //每次fill view后就记录下开始child和结束child的position
    private var mStartPosition: Int = RecyclerView.NO_POSITION
    private var mEndPosition: Int = RecyclerView.NO_POSITION

    init {
        setOrientation(orientation)
    }

    constructor(@RecyclerView.Orientation orientation: Int, canLoop: Boolean) : this(orientation) {
        this.canLoop = canLoop
    }

    fun setLoop(canLoop: Boolean) {
        this.canLoop = canLoop
    }


    fun setOrientation(@RecyclerView.Orientation orientation: Int) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw IllegalArgumentException("invalid orientation:$orientation")
        }
        mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation)
        mOrientation = orientation
        requestLayout()
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally(): Boolean {
        return mOrientation == HORIZONTAL
    }

    override fun canScrollVertically(): Boolean {
        return mOrientation == VERTICAL
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    private fun logChildCount(tag: String, recycler: RecyclerView.Recycler) {
        Log.e(tag, "childCount = $childCount -- scrapSize = ${recycler.scrapList.size}")
        var sb = StringBuilder()
        for (i in 0 until childCount) {
            sb.append(getPosition(getChildAt(i)!!))
            sb.append(",")
        }
        Log.e("positions===>", "$sb")
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        mPendingPosition = RecyclerView.NO_POSITION
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) {
            return
        }
        when {
            mPendingPosition != RecyclerView.NO_POSITION -> {//scrollToPosition的layoutChildren
                mCurrentPosition = mPendingPosition
            }
            isKeyBoardCase() -> {//软键盘弹出收起的layoutChildren
                mCurrentPosition = getFirstPosition()
                mFixOffset = if (mFixOffset == 0) getFixOffset() else mFixOffset
                Log.e(
                    "onLayoutChildren==>",
                    "onLayoutChildren mCurrentPosition=$mCurrentPosition \n mFixOffset=$mFixOffset "
                )
            }
            else -> {//正常的layoutChildren
                mCurrentPosition = 0
            }
        }
        //将所有Item分离至scrap
        detachAndScrapAttachedViews(recycler)
        layoutChunk(recycler, state)
        logChildCount("onLayoutChildren", recycler)
    }

    private fun isKeyBoardCase(): Boolean {
        return mLastScrollDelta != 0 ||
                (mStartPosition != RecyclerView.NO_POSITION
                        && mEndPosition != RecyclerView.NO_POSITION)
    }

    private fun layoutChunk(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        var totalSpace = mOrientationHelper.totalSpace
        mFillAnchor = 0
        //软键盘弹起、收回是恢复到原来位置
        if (mFixOffset != 0) {
            totalSpace += abs(mFixOffset)
            mFillAnchor = mFixOffset
            mFixOffset = 0
        }
        //scrollTopPositoin 定位锚点
        if (mPendingPosition != RecyclerView.NO_POSITION) {
            if (mPendingPosition > mStartPosition) {
                mFillAnchor = totalSpace
            }
            fill(recycler, state, totalSpace, mPendingPosition < mStartPosition)
        } else {
            fill(recycler, state, totalSpace, true)
        }
    }


    private fun fill(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        available: Int,
        isFillEnd: Boolean
    ): Int {
        var remainingSpace = abs(available)
        while (remainingSpace > 0 && mCurrentPosition in 0 until itemCount) {
            val itemView = recycler.getViewForPosition(mCurrentPosition % itemCount)
            if (isFillEnd) {
                mCurrentPosition += 1
            } else {
                mCurrentPosition -= 1
            }
            if (isFillEnd) {//填充尾部
                addView(itemView)
            } else {//填充头部
                addView(itemView, 0)
            }
            measureChildWithMargins(itemView, 0, 0)
            var left = 0
            var top = 0
            var right = 0
            var bottom = 0
            if (mOrientation == VERTICAL) {
                left = paddingLeft
                right = left + mOrientationHelper.getDecoratedMeasurement(itemView)
                if (isFillEnd) {
                    top = mFillAnchor
                    bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(itemView)
                } else {
                    bottom = mFillAnchor
                    top = bottom - mOrientationHelper.getDecoratedMeasurementInOther(itemView)
                }
            } else {
                top = paddingTop
                bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(itemView)
                if (isFillEnd) {
                    left = mFillAnchor
                    right = left + mOrientationHelper.getDecoratedMeasurement(itemView)
                } else {
                    right = mFillAnchor
                    left = right - mOrientationHelper.getDecoratedMeasurement(itemView)
                }
            }
            Log.e("position=>", "mCurrentPosition==>${mCurrentPosition - 1}  ")
            Log.e(
                "layout=>${mCurrentPosition - 1}",
                "left=>$left top=>$top right=>$right bottom=>$bottom "
            )
            layoutDecoratedWithMargins(itemView, left, top, right, bottom)
            if (isFillEnd) {
                mFillAnchor += getOrientationWidth(itemView)
            } else {
                mFillAnchor -= getOrientationWidth(itemView)
            }
            Log.e("layout", "mFillAnchor=>$mFillAnchor")
            remainingSpace -= getOrientationWidth(itemView)
        }
        if (!state.isMeasuring) {
            calcStartEndPosition()
        }
        return available
    }

    private fun calcStartEndPosition() {
        if (childCount == 0) return
        mStartPosition = getPosition(getChildAt(0)!!)
        mEndPosition = getPosition(getChildAt(childCount - 1)!!)
    }


    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //填充view
        val consumed = fillScroll(dx, recycler, state)
        //移动view
        offsetChildrenHorizontal(-consumed)
        //回收view
        recycle(consumed, recycler)
        mLastScrollDelta = consumed
        //输出children
        logChildCount("scrollHorizontallyBy", recycler)
        return consumed
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        //填充view
        val consumed = fillScroll(dy, recycler, state)
        //移动view
        offsetChildrenVertical(-consumed)
        //回收view
        recycle(consumed, recycler)
        mLastScrollDelta = consumed
        //输出children
        logChildCount("scrollVerticallyBy", recycler)
        return consumed

    }

    private fun fillScroll(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        Log.e("fillScroll", "delta==>$delta  state==>${state.remainingScrollHorizontal}")
        return if (delta > 0) {
            fillEnd(delta, recycler, state)
        } else {
            fillStart(delta, recycler, state)
        }

    }

    private fun fillStart(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {

        val anchorView = getChildAt(0)!!
        val anchorStart = mOrientationHelper.getDecoratedStart(anchorView)
        val anchorPosition = getPosition(anchorView)

        //如果anchorView结束的边减去移动的距离还是没出现在屏幕内那么就可以继续滚动，不填充view
        if (anchorStart - delta < mOrientationHelper.startAfterPadding) {
            return delta
        }
        if (!canLoop) {
            //如果 startPosition == 0 且startPosition的开始的边加上移动的距离大于等于Recyclerview的最小宽度或高度，就返回修正过后的移动距离
            if (anchorPosition == 0 && anchorStart - delta >= mOrientationHelper.startAfterPadding) {
                return anchorStart - mOrientationHelper.startAfterPadding
            }
        }
        mFillAnchor = anchorStart
        Log.e("fillStart", "mFillAnchor==>$mFillAnchor  anchorPosition==>$anchorPosition")
        if (canLoop) {
            mCurrentPosition = (anchorPosition - 1) % itemCount
            if (mCurrentPosition < 0) {
                mCurrentPosition += itemCount
            }
        } else {
            mCurrentPosition = anchorPosition - 1
        }
        return fill(recycler, state, delta, false)
    }

    private fun fillEnd(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {

        val anchorView = getChildAt(childCount - 1)!!
        val anchorEnd = mOrientationHelper.getDecoratedEnd(anchorView)
        val anchorPosition = getPosition(anchorView)

        //如果anchorView结束的边减去移动的距离还是没出现在屏幕内那么就可以继续滚动，不填充view
        if (anchorEnd - delta > mOrientationHelper.endAfterPadding) {
            return delta
        }
        if (!canLoop) {
            //如果 endPosition == itemCount - 1 且endView的结束的边减去移动的距离小于等于Recyclerview的最大宽度或高度，就返回修正过后的移动距离
            if (anchorPosition == state.itemCount - 1 && anchorEnd - delta <= mOrientationHelper.endAfterPadding) {
                val diff = anchorEnd - mOrientationHelper.endAfterPadding
                return if (diff > 0) diff else 0
            }
        }
        mFillAnchor = anchorEnd

        if (canLoop) {
            mCurrentPosition = (anchorPosition + 1) % itemCount
            if (mCurrentPosition < 0) {
                mCurrentPosition += itemCount
            }
        } else {
            mCurrentPosition = anchorPosition + 1
        }
        return fill(recycler, state, delta, true)
    }

    /**
     * 获取对应方向的宽度 HORIZONTAL->item宽度 VERTICAL->item高度
     */
    private fun getOrientationWidth(view: View): Int {
        return if (mOrientation == HORIZONTAL) mOrientationHelper.getDecoratedMeasurement(view)
        else mOrientationHelper.getDecoratedMeasurementInOther(view)
    }


    private fun recycle(
        dx: Int,
        recycler: RecyclerView.Recycler
    ) {
        //要回收View的集合，暂存
        val recycleViews = hashSetOf<View>()

        //dx>0就是手指从右滑向左(从下往上)，所以要回收前面的children
        if (dx > 0) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)!!
                val right = getDecoratedRight(child)
                if (mOrientation == HORIZONTAL) {
                    //itemView的right<0就是要超出屏幕要回收View（回收左边的）
                    if (right > 0) break
                }
                if (mOrientation == VERTICAL) {
                    //itemView的bottom<0就是要超出屏幕要回收View（回收上边的）
                    val bottom = getDecoratedBottom(child)
                    if (bottom > 0) break
                }
                recycleViews.add(child)
            }
        }
        //dx<0就是手指从左滑向右(从上往下)，所以要回收后面的children
        if (dx < 0) {
            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i)!!
                if (mOrientation == HORIZONTAL) {
                    val left = getDecoratedLeft(child)
                    //itemView的left>recyclerView.width就是要超出屏幕要回收View(回收右边的)
                    if (left < width) break
                }
                if (mOrientation == VERTICAL) {
                    //itemView的top>recyclerView.height就是要超出屏幕要回收View（回收下边的）
                    val top = getDecoratedTop(child)
                    if (top < height) break
                }
                recycleViews.add(child)
            }
        }
        //真正把View移除掉
        for (view in recycleViews) {
            removeAndRecycleView(view, recycler)
        }
        recycleViews.clear()
    }


    override fun scrollToPosition(position: Int) {
        if (position < 0 || position >= itemCount || itemCount == 0) return
        mPendingPosition = position
        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        Log.e("bannerview", "$position<====$mPendingPosition")
        val linearSmoothScroller =
            LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }
        val firstChildPos = getPosition(getChildAt(0)!!)
        val direction = if (canLoop) 1 else {
            if (targetPosition < firstChildPos) -1 else 1
        }
        return if (mOrientation == HORIZONTAL) {
            PointF(direction.toFloat(), 0f)
        } else {
            PointF(0f, direction.toFloat())
        }
    }

    private fun getFixOffset(): Int {
        if (childCount == 0) return 0
        return mOrientationHelper.getDecoratedStart(getStartView())
    }

    private fun getStartView(): View {
        return getChildAt(0)!!
    }

    private fun getEndView(): View {
        return getChildAt(childCount - 1)!!
    }

    private fun getFirstPosition(): Int {
        return getPosition(getStartView())
    }

    private fun getEndPosition(): Int {
        return getPosition(getEndView())
    }

    fun reset() {
        mStartPosition = NO_POSITION
        mEndPosition = NO_POSITION
    }
}