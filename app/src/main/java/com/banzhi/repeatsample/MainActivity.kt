package com.banzhi.repeatsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banzhi.repeat.RepeatLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

        private lateinit var layoutManager: RepeatLayoutManager
        private lateinit var layoutManager0: LinearLayoutManager
//    private lateinit var layoutManager: StackLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layoutManager0= LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
        recyclerView0.layoutManager =layoutManager0
//        recyclerView.layoutManager =  RepeatLayoutManager2(RecyclerView.HORIZONTAL)
        layoutManager = RepeatLayoutManager(RecyclerView.HORIZONTAL)
//        layoutManager = StackLayoutManager(LinearLayoutManager.HORIZONTAL, false, 0, false)
        recyclerView.layoutManager = layoutManager
        recyclerView0.adapter = SampleAdapter(getDatas())
        recyclerView.adapter = SampleAdapter(getDatas())
        btn.setOnClickListener {
            val res = edit.text.toString()
            recyclerView.smoothScrollToPosition(res.toInt())
            recyclerView0.smoothScrollToPosition(res.toInt())
//            layoutManager.scrollToPosition(res.toInt())
//            layoutManager0.scrollToPosition(res.toInt())
        }

    }

    private fun getDatas(): MutableList<String>? {
        val datas = mutableListOf<String>()
        for (i in 0..21) {
            datas.add("${i}pos")
        }
        return datas
    }
}
