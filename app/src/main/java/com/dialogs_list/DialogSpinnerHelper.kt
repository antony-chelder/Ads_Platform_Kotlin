package com.dialogs_list

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utils.CityObject
import com.dialogs.RcViewSpinnerAdapter
import com.tony_fire.descorderkotlin.R

class DialogSpinnerHelper {
    fun showspinnerdialog(context: Context,list:ArrayList<String>,selectionText:TextView){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rootview = LayoutInflater.from(context).inflate(R.layout.spinner_layout,null)
        val adapter = RcViewSpinnerAdapter(selectionText,dialog)
        val rc_view = rootview.findViewById<RecyclerView>(R.id.rc_sp_view)
        val sw_view = rootview.findViewById<SearchView>(R.id.svSpinner)
        rc_view.layoutManager = LinearLayoutManager(context)
        rc_view.adapter = adapter
        adapter.UpdateAdapter(list)
        dialog.setView(rootview)
        setSearchView(adapter,list,sw_view)
        dialog.show()



    }

    private fun setSearchView(adapter: RcViewSpinnerAdapter, list: ArrayList<String>, swView: SearchView?) {
        swView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {

          return false
            }

            override fun onQueryTextChange(newtext: String?): Boolean {
                val tempList = CityObject.filterListData(list,newtext)
                adapter.UpdateAdapter(tempList)
               return true
            }
        })

    }

}