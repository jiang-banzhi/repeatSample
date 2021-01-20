package com.banzhi.repeat.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 *<pre>
 * @author : jiang
 * @time : 2021/1/19.
 * @desciption :
 * @version :
 *</pre>
 */
abstract class Holder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        initView(itemView)
    }

    abstract fun initView(itemView: View)

    abstract fun updateUI(t: T)
}