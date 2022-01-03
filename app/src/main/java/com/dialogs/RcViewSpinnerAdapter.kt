package com.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.act.EditAdsActivity
import com.tony_fire.descorderkotlin.R

class RcViewSpinnerAdapter( var selectionText:TextView,var dialog:AlertDialog): RecyclerView.Adapter<RcViewSpinnerAdapter.SpViewHolder>() {
    private val sp_item_list = ArrayList<String>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item,parent,false)
        return SpViewHolder(view,selectionText,dialog)

    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.Setdata(sp_item_list[position])

    }

    override fun getItemCount(): Int {
        return sp_item_list.size

    }
    class SpViewHolder(itemView: View, var selectionText:TextView,  var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        private var itexText = ""

        fun Setdata(text:String){
            val tv_sp_item = itemView.findViewById<TextView>(R.id.tv_sp_item)
            itexText = text
            tv_sp_item.text = text
            itemView.setOnClickListener(this)

        }

        override fun onClick(p0: View?) {
          selectionText.text = itexText
            dialog.dismiss()

        }

    }
    fun UpdateAdapter(list:ArrayList<String>){
        sp_item_list.clear()
        sp_item_list.addAll(list)
        notifyDataSetChanged()

    }

}