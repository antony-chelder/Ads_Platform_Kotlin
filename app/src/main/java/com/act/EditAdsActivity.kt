package com.act

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.utils.CityObject
import com.utils.ImageManager
import com.utils.ImagePixPicker
import com.adapter.ImageAdapter
import com.model.AdPost
import com.model.DbManager
import com.dialogs_list.DialogSpinnerHelper
import com.fragments.FragmentCloseInterface
import com.fragments.ImageListFrag
import com.google.android.gms.tasks.OnCompleteListener
import com.tony_fire.descorderkotlin.MainActivity
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.ActivityEditAdsBinding
import java.io.ByteArrayOutputStream


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseimagwFrag: ImageListFrag? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var editImagePos = 0
    private var imageIndex = 0
    private var iseditstate = false
    private var ad : AdPost? = null
    private val dbManager = DbManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        checkEditState()
        imageChangeCounter()

    }

    private fun checkEditState(){
        if(isEditState()){
            iseditstate = true
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as AdPost
           if(ad!=null) fillAdsView(ad!!)
        }
    }

    private fun isEditState() : Boolean{
        return intent.getBooleanExtra(MainActivity.EditState,false)
    }

    private fun fillAdsView(ad: AdPost) = with(binding){
             edTitle.setText(ad.title)
        selectCountry.text = ad.country
        selectCity.text = ad.city
        edTel.setText(ad.tel)
        edEmail.setText(ad.email)
        edIndex.setText(ad.index)
        checkSend.isChecked = ad.withsend.toBoolean()
        selectCategory.text = ad.category
        edPrice.setText(ad.price)
        edDesc.setText(ad.description)

        ImageManager.FillImageArray(ad,imageAdapter) 

    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImage.adapter = imageAdapter
        OnclickSelectCountry()
        OnclickSelectCity()
        OnclickSelectCategory()


    }



    fun OnclickSelectCategory() = with(binding) {
        selectCategory.setOnClickListener {
            val listcategory = resources.getStringArray(R.array.category_array).toMutableList() as ArrayList
            dialog.showspinnerdialog(this@EditAdsActivity, listcategory,selectCategory)
        }


    }

    private fun OnclickSelectCity() = with(binding) {
        selectCity.setOnClickListener {
            val selectedCountry = selectCountry.text.toString()
            val listcity = CityObject.getAllCities(selectedCountry, this@EditAdsActivity)
            if (selectedCountry != getString(R.string.selectcountry)) {
                dialog.showspinnerdialog(this@EditAdsActivity, listcity,selectCity)
            } else {
                Toast.makeText(this@EditAdsActivity, "No country selected", Toast.LENGTH_LONG).show()
            }
        }

    }


    private fun OnclickSelectCountry() = with(binding) {
        selectCountry.setOnClickListener {
            val listcountries = CityObject.getAllCountries(this@EditAdsActivity)
            if (selectCity.text.toString() != getString(R.string.selectсity)) {
                selectCity.text = getString(R.string.selectсity)
            }
            dialog.showspinnerdialog(this@EditAdsActivity, listcountries, binding.selectCountry)
        }

    }
    private fun imageChangeCounter(){ 
        binding.vpImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ 
            override fun onPageSelected(position: Int) { 
                super.onPageSelected(position)
                val imagecounter = "${position + 1}/${binding.vpImage.adapter?.itemCount}" 
                binding.imageCounter.text = imagecounter 
            }

        })

    }

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainarraypager.size == 0) {
            ImagePixPicker.getMultiImages(this,4)
        } else {
            OpenChoosedImage(null)
            chooseimagwFrag?.updateAdapterFromEdit(imageAdapter.mainarraypager)
        }

    }
    fun onClickPublishPost(view: View) {
       ad =  FillAdPost()
        if(iseditstate) {
            ad?.copy(key = ad?.key)?.let { dbManager.publishAd(it, onPublickFinish()) }
        } else {
            uploadImages() 
        }
    }

    private fun onPublickFinish() : DbManager.FinishworkListener{
        return object : DbManager.FinishworkListener{
            override fun onFinish() {
                finish()
            }

        }

    }

     private fun FillAdPost() : AdPost{
        val adTemp : AdPost
        binding.apply {
            adTemp = AdPost(selectCountry.text.toString(),
                selectCity.text.toString(),edTel.text.toString(),
                edIndex.text.toString(),  edEmail.text.toString(),
                checkSend.isChecked.toString(),
                selectCategory.text.toString(),
                edPrice.text.toString(),
                ad?.mainImage?:"empty",
                ad?.image2?:"empty",
                ad?.image3?:"empty",
                ad?.image4?:"empty",
                false,
                edDesc.text.toString(),
                ad?.key?: dbManager.db.push().key,
                "0",edTitle.text.toString(),
                dbManager.auth.uid,
                ad?.time?:System.currentTimeMillis().toString())

        }
        return adTemp
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewEdit.visibility = View.VISIBLE
        imageAdapter.updateAdapter(list)
        chooseimagwFrag = null

    }

    fun OpenChoosedImage(newlist: ArrayList<Uri>?) {
        chooseimagwFrag = ImageListFrag(this)
        if(newlist != null)chooseimagwFrag?.resizeSelectedImages(newlist,true,this) 
        binding.scrollViewEdit.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_for_images, chooseimagwFrag!!)
        fm.commit()

    }

    private  fun uploadImages(){ 
        if(imageAdapter.mainarraypager.size == imageIndex){ 
            dbManager.publishAd(ad!!, onPublickFinish()) 
            return

        }
      val byteArray =  prepareImageByteArray(imageAdapter.mainarraypager[imageIndex]) 
        uploadImage(byteArray){ 

            nextImagesCount(it.result.toString()) 
        }

    }

    private fun nextImagesCount(uri: String){ 
        setUriToAdd(uri) 
        imageIndex++ 
        uploadImages()
    }

    private fun setUriToAdd(uri : String){ 
        when(imageIndex){ 
            0 -> ad = ad?.copy(mainImage = uri) 
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
            3 -> ad = ad?.copy(image4 = uri)
        }
    }

    private fun prepareImageByteArray(bitmap: Bitmap) : ByteArray{ 
        val outStream = ByteArrayOutputStream() 
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,outStream) 
        return outStream.toByteArray() 


    }

    private  fun uploadImage(byteArray: ByteArray, listener : OnCompleteListener<Uri>){ 
       val imStorageRef = dbManager.storage
           .child(dbManager.auth.uid!!)
           .child("image_${System.currentTimeMillis()}")  

        val uploadTak = imStorageRef.putBytes(byteArray) 
        uploadTak.continueWithTask{ 
            task-> imStorageRef.downloadUrl 

        }.addOnCompleteListener(listener) 
    }


}
