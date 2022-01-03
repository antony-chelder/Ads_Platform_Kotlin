package com.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.utils.ImageManager
import com.utils.ImagePixPicker
import com.act.EditAdsActivity
import com.dialogs.ProgressDialog
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.ListImageFragBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag( private val FragCloseInterface:FragmentCloseInterface): BaseAdsFragment(),AdapterCallBack{
    val adapter = SelectImageRVAdapter(this)
    lateinit var binding : ListImageFragBinding
    val dragCallback = ItemTouchMoveCallBack(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var add_image : MenuItem? = null
    private  var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            touchHelper.attachToRecyclerView(rcImage)
            setFragToolbar()
            rcImage.layoutManager = LinearLayoutManager(activity)
                rcImage.adapter = adapter
        }

    }

     fun resizeSelectedImages(newlist: ArrayList<Uri>,needclear :Boolean,activity:Activity){
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmaplist = ImageManager.ImageResize(newlist,activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmaplist, needclear)
            if(adapter.mainImageArray.size > 3) add_image?.isVisible = false
        }
    }
    override fun onItemDeleted() {
        add_image?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmaplist :  List<Bitmap>){
        adapter.updateAdapter(bitmaplist, false)
    }

    override fun onClose() { // Запускается только когда нажимается стрелка назад
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit()
        FragCloseInterface.onFragClose(adapter.mainImageArray)
        job?.cancel()
    }

    override fun onDetach() { // Метод запускается когда фрагмент закрываем или когда один фрагмент заменяем другим
        super.onDetach()


    }
    private fun setFragToolbar() {
        binding.apply {
            toolbarFrag.inflateMenu(R.menu.frag_menu)
            val deleteitem = toolbarFrag.menu.findItem(R.id.id_delete)
            add_image = toolbarFrag.menu.findItem(R.id.id_add_image)
            if(adapter.mainImageArray.size > 3) add_image?.isVisible = false

            toolbarFrag.setNavigationOnClickListener {
                showInterAd()

            }

            deleteitem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                add_image?.isVisible = true
                true


            }
            add_image?.setOnMenuItemClickListener {
                val imagecount = ImagePixPicker.MAX_IMAGE_COUNT - adapter.mainImageArray.size
                ImagePixPicker.addImages(activity as EditAdsActivity,imagecount)
                true
            }

        }
    }

    fun updateAdapter(newList: ArrayList<Uri>,activity: Activity){
     resizeSelectedImages(newList,false,activity)

    }
    fun setSingleImage(uri:Uri,position: Int){
        val p_bar = binding.rcImage[position].findViewById<ProgressBar>(R.id.pBar)
        job =  CoroutineScope(Dispatchers.Main).launch {
            p_bar.visibility = View.VISIBLE
            val bitmaplist = ImageManager.ImageResize(arrayListOf(uri),activity as Activity)
            p_bar.visibility = View.GONE
            adapter.mainImageArray[position] = bitmaplist[0]
            adapter.notifyItemChanged(position)
        }



    }


}