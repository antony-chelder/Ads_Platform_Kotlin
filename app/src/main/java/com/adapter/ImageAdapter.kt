package com.adapter

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tony_fire.descorderkotlin.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainarraypager = ArrayList<Bitmap>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item,parent,false)
        return ImageHolder(view)

    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainarraypager[position])

    }

    override fun getItemCount(): Int {
       return mainarraypager.size

    }
    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var im_item : ImageView

        fun setData(uri:Bitmap){
            im_item = itemView.findViewById(R.id.im_viewpager)
            im_item.setImageBitmap(uri)

        }
    }
    fun updateAdapter(newlist:ArrayList<Bitmap>){
        mainarraypager.clear()
        mainarraypager.addAll(newlist)
        notifyDataSetChanged()

    }
}