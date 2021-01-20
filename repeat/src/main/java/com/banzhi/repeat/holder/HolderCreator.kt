package com.banzhi.repeat.holder

import android.view.View

/**
 *<pre>
 * @author : jiang
 * @time : 2021/1/19.
 * @desciption :
 * @version :
 *</pre>
 */
interface HolderCreator<T> {
    fun createHolder(itemView: View): Holder<T>
    fun layoutId(): Int

}