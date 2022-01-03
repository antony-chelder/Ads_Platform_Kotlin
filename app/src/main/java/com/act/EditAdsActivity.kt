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

        ImageManager.FillImageArray(ad,imageAdapter) // При заполнении данных которые приходят, также передача картинок

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
    private fun imageChangeCounter(){ // Счетчик картинок, при перелистовании
        binding.vpImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ // Создан колбэк которые прослушивает перелистование в ViewPager
            override fun onPageSelected(position: Int) { // Показыввает позицию где мы остановились в перелистовании
                super.onPageSelected(position)
                val imagecounter = "${position + 1}/${binding.vpImage.adapter?.itemCount}" // Увеличение количества значения на + 1 так как позиция идет с 0, также подсчет количества фото в адаптере
                binding.imageCounter.text = imagecounter // Передача созданного значение в текст
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
            uploadImages() // Сначала после нажатия на кнопку публикация, будут загружатся картинки
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
        val ad : AdPost
        binding.apply {
            ad = AdPost(selectCountry.text.toString(),selectCity.text.toString(),edTel.text.toString(),
                edIndex.text.toString(),  edEmail.text.toString(),checkSend.isChecked.toString(),
                selectCategory.text.toString(),edPrice.text.toString(),"","","","",false,edDesc.text.toString(),dbManager.db.push().key,"0",edTitle.text.toString(),dbManager.auth.uid,System.currentTimeMillis().toString())

        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewEdit.visibility = View.VISIBLE
        imageAdapter.updateAdapter(list)
        chooseimagwFrag = null

    }

    fun OpenChoosedImage(newlist: ArrayList<Uri>?) {
        chooseimagwFrag = ImageListFrag(this)
        if(newlist != null)chooseimagwFrag?.resizeSelectedImages(newlist,true,this) // Проверка, чтобы старый список не перезаписовал новый
        binding.scrollViewEdit.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_for_images, chooseimagwFrag!!)
        fm.commit()

    }

    private  fun uploadImages(){ // Функция для загрузки всех фото на Firebase Storage
        if(imageAdapter.mainarraypager.size == imageIndex){ // Условие для подсчета количества картинок которые нам пришли
            dbManager.publishAd(ad!!, onPublickFinish()) // Происходит публикация, записуется ссылка картинки
            return

        }
      val byteArray =  prepareImageByteArray(imageAdapter.mainarraypager[imageIndex]) // Берем массив с байтами с адаптера где отображаются добавленные фото
        uploadImage(byteArray){ // Полная подготовка uri ссылок и после того как загрузились картинки, загружается текстовая часть

            nextImagesCount(it.result.toString()) // Ссылка на картинку(и) после публикации
        }

    }

    private fun nextImagesCount(uri: String){ // В этой функции берем со следущей позиции в адаптере и заного запускаем функцию загрузки
        setUriToAdd(uri) // Перед тем как увеличивать, запуск проверки на количество фото
        imageIndex++ // Увеличение на 1, берем со следующей позиции с адаптера
        uploadImages()
    }

    private fun setUriToAdd(uri : String){ // Проверка на количество загруженных ссылок в адаптере
        when(imageIndex){ // По индексу проверка
            0 -> ad = ad?.copy(mainImage = uri) // Если на первой позиции значит загружена 1 фотографию, и копируем это значение в переменную
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
            3 -> ad = ad?.copy(image4 = uri)
        }
    }

    private fun prepareImageByteArray(bitmap: Bitmap) : ByteArray{ // Функция для того чтобы по очереди с Bitmap превращать картинки в ByteArray массив
        val outStream = ByteArrayOutputStream() // Подготовка outputStream
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,outStream) // Сжимаем в опредленный формат фото, выставляем качество, когда сожмется, превратится в outputStream
        return outStream.toByteArray() // Превращаем с потока в byteArray , так как Firebase принимает обьекты в байтах


    }

    private  fun uploadImage(byteArray: ByteArray, listener : OnCompleteListener<Uri>){ // Функция для загрузки каждого фото по очереди, передается listener, чтобы отслеживать когда закончится загрузка, это будет на второстепенном потоке, чтобы не перезагружать основной
       val imStorageRef = dbManager.storage
           .child(dbManager.auth.uid!!)
           .child("image_${System.currentTimeMillis()}")  // Формируем ссылку на картинку по пути, где картинка будет добавлятся по времени

        val uploadTak = imStorageRef.putBytes(byteArray) // Загрузка фото по созданному пути
        uploadTak.continueWithTask{ // Сохранение ссылки на картинку, по которой она будет отображатся пользователям
            task-> imStorageRef.downloadUrl // Скачивание ссылки на картинку которую только что загрузили

        }.addOnCompleteListener(listener) // Когда загрузили, запускаем interface который будет передаватся в функции UploadImages
    }


}