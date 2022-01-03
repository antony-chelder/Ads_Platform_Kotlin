package com.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utils.ImageManager
import com.utils.ImagePixPicker
import com.act.EditAdsActivity
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.SelectImageFragItemBinding

class SelectImageRVAdapter(val adaptercallBack : AdapterCallBack): RecyclerView.Adapter<SelectImageRVAdapter.ImageHolder>(),ItemTouchMoveCallBack.IteamTouchAdapter {
    val mainImageArray = ArrayList<Bitmap>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding =  SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ImageHolder(viewBinding,parent.context,this)

    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.SetData(mainImageArray[position])

    }

    override fun getItemCount(): Int {
       return mainImageArray.size

    }
    override fun onMove(firstPos: Int, targetPos: Int) {
        val targetItem = mainImageArray[targetPos]
        mainImageArray[targetPos] = mainImageArray[firstPos]
        mainImageArray[firstPos] = targetItem
        notifyItemMoved(firstPos,targetPos)


    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder( private val viewBinding: SelectImageFragItemBinding, val contex:Context, val adapter:SelectImageRVAdapter) : RecyclerView.ViewHolder(viewBinding.root) {



        fun SetData(bitmap:Bitmap) {
            viewBinding.apply {
                imEdit.setOnClickListener {
                  ImagePixPicker.getSingleImage(contex as EditAdsActivity)
                    contex.editImagePos = adapterPosition
                }
                imDelete.setOnClickListener {
                    adapter.mainImageArray.removeAt(adapterPosition)
                    adapter.notifyItemRemoved(adapterPosition)
                    for (n in 0 until adapter.mainImageArray.size) adapter.notifyItemChanged(n)
                    adapter.adaptercallBack.onItemDeleted()

                }

                imageTitle.text = contex.resources.getStringArray(R.array.image_title_array)[adapterPosition]
                ImageManager.chooseScaleType(imageContent,bitmap)
                imageContent.setImageBitmap(bitmap)

            }
        }

    }
    fun updateAdapter(newlist:List<Bitmap>,needclear:Boolean){
        if(needclear)mainImageArray.clear()
        mainImageArray.addAll(newlist)
        notifyDataSetChanged()
    }




}