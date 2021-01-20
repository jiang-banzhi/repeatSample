package com.banzhi.repeat.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.banzhi.repeat.`interface`.OnItemClickListener
import com.banzhi.repeat.`interface`.OnPageChangeListener
import com.banzhi.repeat.adapter.BannerAdapter
import com.banzhi.repeat.holder.HolderCreator
import java.util.*


/**
 * <pre>
 * @author : jiang
 * @time : 2021/1/15.
 * @desciption :
 * @version :
</pre> *
 */
class BannerView : FrameLayout, LifecycleObserver {

    companion object {
        private const val TURING = 0x703
    }


    /**
     * 自动滚动的间隔时间
     */
    private var autoTime: Long = 1000
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: RepeatLayoutManager
    private lateinit var snapHelper: RepeatPagerSnapHelper
    private lateinit var pointViews: RadioGroup
    /**
     * 是否自动滚动
     */
    private var isAuto = false
    private var indicators = arrayOf(2)
    /**
     * 获取当前位置
     *
     * @return
     */
    /**
     * 设置当前位置
     * @param position
     */
    var currentPosition: Int
        get() {
            val snapView = snapHelper.findSnapView(mLayoutManager) ?: return 0
            return mLayoutManager.getPosition(snapView)
        }
        private set(position) = mLayoutManager.scrollToPosition(position)

    /**
     * 是否开启自动轮播
     */
    private var isTurning = false

    private var onPageSelectListener: OnPageChangeListener? = null
    private var onItemClickListener: OnItemClickListener? = null
    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            if (what == TURING) {
                val page = (currentPosition + 1) % mLayoutManager.itemCount
                smoothScrollToPosition(page)
                startTurning()
                Log.e("BannerView", "$page<====")
                Log.e("BannerView", Date().toString())
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null, -1)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, -1)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    enum class IndicatorAlign {
        TOP_CENTER, BOTTOM_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        mRecyclerView = RecyclerView(context)
        mLayoutManager = RepeatLayoutManager(RecyclerView.HORIZONTAL)
        mRecyclerView.layoutManager = mLayoutManager
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        snapHelper = RepeatPagerSnapHelper()
        snapHelper.attachToRecyclerView(mRecyclerView)
        addView(mRecyclerView, params)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    onPageSelectListener?.onPageSelect(currentPosition)
                    setIndicatorSelect(currentPosition)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onPageSelectListener?.onScrolled(recyclerView, dx, dy)
            }
        })
        pointViews = RadioGroup(context)
        val pointsParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        pointsParams.gravity = Gravity.CENTER or Gravity.BOTTOM
        pointViews.layoutParams = pointsParams
        pointViews.setPadding(24, 6, 24, 6)
        pointViews.orientation = LinearLayout.HORIZONTAL
        addView(pointViews, pointsParams)
        pointViews.setOnCheckedChangeListener { group, checkedId ->
            val indexOfChild = group.indexOfChild(group.findViewById(checkedId))
            setCurrentPosition(indexOfChild, true)
        }
    }


    fun setOrientation(@RecyclerView.Orientation orientation: Int) {
        mLayoutManager.setOrientation(orientation)
    }

    /**
     * 设置是否循环
     *
     * @param canLoop
     */
    fun setCanLoop(canLoop: Boolean) {
        mLayoutManager.setLoop(canLoop)
    }


    /**
     * 设置当前位置
     *
     * @param position
     * @param smoothScroll
     */
    fun setCurrentPosition(position: Int, smoothScroll: Boolean) {
        if (smoothScroll) {
            smoothScrollToPosition(position)
        } else {
            currentPosition = position
        }
    }

    /**
     * 滚动到指定position
     * @param postion
     */
    fun smoothScrollToPosition(postion: Int) {
        mRecyclerView.smoothScrollToPosition(postion)
    }

    @JvmOverloads
    fun startTurning(time: Long = autoTime) {
        if (time < 0) {
            return
        }
        this.autoTime = time
        isAuto = true
        if (isTurning) {
            stopTurning()
        }
        isTurning = true
        mHandler.sendEmptyMessageDelayed(TURING, autoTime)
    }

    fun stopTurning() {
        mHandler.removeMessages(TURING)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
            // 开始翻页
            if (isAuto) {
                startTurning(autoTime)
            }
        } else if (action == MotionEvent.ACTION_DOWN) {
            // 停止翻页
            if (isAuto) {
                stopTurning()
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    fun setOnPageSelectListener(onPageSelectListener: OnPageChangeListener) {
        this.onPageSelectListener = onPageSelectListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
        setOnItemClickListener()
    }

    private fun setOnItemClickListener() {
        onItemClickListener?.let { (mRecyclerView.adapter as BannerAdapter<*>?)?.setOnItemClickListener(it) }
    }

    /**
     *设置轮播数据
     */
    fun <T> setBanners(creator: HolderCreator<T>, datas: MutableList<T>) {
        val mAdapter = BannerAdapter(datas, creator)
        mRecyclerView.adapter = mAdapter
        setOnItemClickListener()
        initIndicator()
    }

    fun setPageIndicator(indicators: Array<Int>) {
        this.indicators = indicators
    }

    fun setPageIndicatorAlign(align: IndicatorAlign) {
        val layoutParams = pointViews.layoutParams as LayoutParams
        when (align) {
            IndicatorAlign.TOP_CENTER -> {
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER
            }
            IndicatorAlign.BOTTOM_CENTER -> {
                layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER
            }
            IndicatorAlign.TOP_LEFT -> {
                layoutParams.gravity = Gravity.TOP or Gravity.START
            }
            IndicatorAlign.BOTTOM_LEFT -> {
                layoutParams.gravity = Gravity.BOTTOM or Gravity.START
            }
            IndicatorAlign.TOP_RIGHT -> {
                layoutParams.gravity = Gravity.TOP or Gravity.END
            }
            IndicatorAlign.BOTTOM_RIGHT -> {
                layoutParams.gravity = Gravity.BOTTOM or Gravity.END
            }
        }
        pointViews.layoutParams = layoutParams

    }

    private fun initIndicator() {
        if (indicators.isNullOrEmpty()) {
            return
        }
        val count = mLayoutManager.itemCount
        if (count == 0) {
            return
        }
        for (index in 0 until count) {
            val pointView = RadioButton(context)
            val params = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            params.setMargins(3, 0, 3, 0)
            pointView.layoutParams = params
            pointView.buttonDrawable = context.resources.getDrawable(indicators[0])
            pointViews.addView(pointView, params)
        }
        setIndicatorSelect(currentPosition)
    }

    private fun setIndicatorSelect(index: Int) {
        (pointViews.getChildAt(index) as RadioButton).isChecked = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        if (isAuto) {
            stopTurning()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        if (isAuto) {
            startTurning()
        }
    }


}
