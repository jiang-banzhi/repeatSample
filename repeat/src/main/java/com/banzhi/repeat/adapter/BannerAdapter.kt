package com.banzhi.repeat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.banzhi.repeat.`interface`.OnItemClickListener
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
class BannerAdapter<T>(datas: MutableList<T>, creator: HolderCreator<T>) : RecyclerView.Adapter<Holder<T>>() {

    private var mDatas: MutableList<T>? = datas
    private var mCreator: HolderCreator<T>? = creator
    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<T> {
        val itemView = LayoutInflater.from(parent.context).inflate(mCreator?.layoutId() ?: -1, parent, false)
        return mCreator!!.createHolder(itemView)
    }

    override fun getItemCount(): Int = mDatas?.size ?: 0

    override fun onBindViewHolder(holder: Holder<T>, position: Int) {
        mDatas?.get(position)?.let { holder.updateUI(it) }
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClickListener(position)
        }
    }


}