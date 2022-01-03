package com.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import com.act.EditAdsActivity
import com.tony_fire.descorderkotlin.R
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePixPicker {
    const val MAX_IMAGE_COUNT = 4;

    private fun getOptions(imagecounter: Int): Options {
        val options: Options = Options().apply {
            count = imagecounter
            isFrontFacing = false
            mode = Mode.Picture
            videoDurationLimitInSeconds = 30
            path = "/pix/images"

        }
        return options
    }

    fun getMultiImages(  
        edact: EditAdsActivity,
        imagecounter: Int
    ) {
        edact.addPixToActivity(R.id.place_for_images, getOptions(imagecounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectedImages(edact, result.data) 

                }
            }
        }
    }

    fun addImages(  
        edact: EditAdsActivity,
        imagecounter: Int
    ) {
        edact.addPixToActivity(R.id.place_for_images, getOptions(imagecounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    Log.d("MyLog", "Done")
                    openChooseImageFrag(edact)
                    edact.chooseimagwFrag?.updateAdapter(result.data as ArrayList<Uri>,edact)


                }
            }
        }
    }

    fun getSingleImage(  
        edact: EditAdsActivity
    ) {
        edact.addPixToActivity(R.id.place_for_images, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edact)
                   SingleImages(edact,result.data[0]) //

                }
            }
        }
    }

    private fun openChooseImageFrag(edact: EditAdsActivity){ 
        edact.supportFragmentManager.beginTransaction().replace(R.id.place_for_images,edact.chooseimagwFrag!!).commit() 

    }

    private fun closePixFrag(edact: EditAdsActivity) { 
        val fList = edact.supportFragmentManager.fragments
        fList.forEach { frag ->
            if (frag.isVisible) {
                edact.supportFragmentManager.beginTransaction().remove(frag).commit()
            }
        }
    }

    fun getMultiSelectedImages(
        edact: EditAdsActivity,
        uris: List<Uri>
    ) {

        if (uris.size > 1 && edact.chooseimagwFrag == null) {
            edact.OpenChoosedImage(uris as ArrayList<Uri>)

        }  else if (uris.size == 1 && edact.chooseimagwFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edact.binding.pbEdit.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.ImageResize(uris as ArrayList<Uri>, edact) as ArrayList<Bitmap>
                edact.binding.pbEdit.visibility = View.GONE
                edact.imageAdapter.updateAdapter(bitmapArray)
                closePixFrag(edact)
            }
        }

    }


    private fun SingleImages(edact: EditAdsActivity, uri : Uri){ //

                   edact.chooseimagwFrag?.setSingleImage(uri, edact.editImagePos)


            }
}
