package com.banzhi.repeatsample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.banzhi.repeat.`interface`.OnItemClickListener
import com.banzhi.repeat.`interface`.OnPageChangeListener
import com.banzhi.repeat.widget.BannerView
import com.banzhi.repeat.widget.RepeatLayoutManager
import com.banzhi.repeat.widget.RepeatPagerSnapHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var layoutManager: RepeatLayoutManager
    private lateinit var layoutManager0: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layoutManager0 = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
        recyclerView0.layoutManager = layoutManager0
//        recyclerView.layoutManager =  RepeatLayoutManager2(RecyclerView.HORIZONTAL)
        layoutManager = RepeatLayoutManager(RecyclerView.HORIZONTAL)
        layoutManager.setLoop(true)
        recyclerView.layoutManager = layoutManager
        recyclerView0.adapter = SampleAdapter(getDatas())
        recyclerView.adapter = SampleAdapter(getDatas())
        val snapHelper = RepeatPagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        PagerSnapHelper().attachToRecyclerView(recyclerView0)
        btn.setOnClickListener {
//            val res = edit.text.toString()
//            recyclerView.smoothScrollToPosition(res.toInt())
//            recyclerView0.smoothScrollToPosition(res.toInt())
//            layoutManager.scrollToPosition(res.toInt())
//            layoutManager0.scrollToPosition(res.toInt())
            update()
        }
        bannerView.setCanLoop(true)
        bannerView.startTurning(5000)
        bannerView.setOnPageSelectListener(object : OnPageChangeListener {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }

            override fun onPageSelect(position: Int) {
//                Toast.makeText(this@MainActivity, "滑动的当前位置==>$position", Toast.LENGTH_SHORT).show()
            }

        })
        bannerView.setIndicatorDrawable(R.drawable.dot_select, R.drawable.dot_unselect)
        bannerView.setPageIndicatorAlign(BannerView.IndicatorAlign.BOTTOM_RIGHT)
        bannerView.setBanners(TestCreator(), getDatas())
        bannerView.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClickListener(position: Int) {
                Toast.makeText(this@MainActivity, "点击当前位置==>$position", Toast.LENGTH_SHORT).show()
            }

        })

        lifecycle.addObserver(bannerView)

    }
    private fun update(){
        bannerView.setBanners(TestCreator(), getDatas2())
    }

    private fun getDatas(): MutableList<String> {
        val datas = mutableListOf<String>()
//        for (i in 0..1) {
//            datas.add("${i}pos")
            datas.add("${0}pos")
//        }
        return datas
    }
    private fun getDatas2(): MutableList<String> {
        val datas = mutableListOf<String>()
        for (i in 0..4) {
            datas.add("${i}pos")
        }
        return datas
    }
}
