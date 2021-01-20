package com.banzhi.repeatsample

import android.view.View
import android.widget.TextView
import com.banzhi.repeat.holder.Holder
import com.banzhi.repeat.holder.HolderCreator

/**
 *<pre>
 * @author : jiang
 * @time : 2021/1/19.
 * @desciption :
 * @version :
 *</pre>
 */
class TestCreator : HolderCreator<String> {

    override fun layoutId(): Int = R.layout.item_sample

    override fun createHolder(itemView: View): Holder<String> {
        return TestViewHolder(itemView)
    }

    inner class TestViewHolder<String>(itemView: View) : Holder<String>(itemView) {
        lateinit var textView: TextView

        override fun updateUI(text: String) {
            textView.setText(text.toString())

        }

        override fun initView(itemView: View) {
            textView = itemView.findViewById(R.id.textView)

        }


    }
}