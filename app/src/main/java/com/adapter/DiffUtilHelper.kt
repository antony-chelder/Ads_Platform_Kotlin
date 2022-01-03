package com.adapter

import androidx.recyclerview.widget.DiffUtil
import com.model.AdPost

class DiffUtilHelper( val oldlist : List<AdPost>, val newlist : List<AdPost>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldlist.size

    }

    override fun getNewListSize(): Int {
        return newlist.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        return  oldlist[oldItemPosition].key == newlist[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return  oldlist[oldItemPosition] == newlist[newItemPosition]
    }
}