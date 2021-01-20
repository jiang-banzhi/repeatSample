package com.banzhi.repeat.`interface`

import androidx.recyclerview.widget.RecyclerView

/**
 *<pre>
 * @author : jiang
 * @time : 2021/1/18.
 * @desciption :
 * @version :
 *</pre>
 */
interface OnPageChangeListener {
    fun onPageSelect(position: Int)
    fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
}