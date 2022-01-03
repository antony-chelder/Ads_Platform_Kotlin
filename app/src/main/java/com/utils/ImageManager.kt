package com.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.adapter.ImageAdapter
import com.model.AdPost
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

object ImageManager {
    const val MAX_IMAGE_SIZE = 1000
    fun getImageSize(uri: Uri, act : Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)  // Получаем inputStream из contentResolver, открываем поток и передаем ссылку которая приходит из Pix, чтобы добратся до файла


//        val fTemp = File(act.cacheDir,"temp.tmp") // Создаем временный файл, и то где он сохранится, также название
//        if (inStream != null) {
//            fTemp.copyInputStreamtoFile(inStream) // Копируем в наш File готовый поток, с помощью функции extensions
//        }


        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
//        BitmapFactory.decodeFile(fTemp.path, options) // Передали путь

        BitmapFactory.decodeStream(inStream,null, options) // Передали поток напрямую


//        return if (ImageRotatiton(fTemp) == 90) // Уже напрямую передается файл
//            listOf(options.outHeight, options.outWidth)
//        else listOf(options.outWidth, options.outHeight)

        return listOf(options.outWidth, options.outHeight)


    }

    fun chooseScaleType(im : ImageView, bitmap: Bitmap){
        if(bitmap.width > bitmap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else{
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

    }

//   private fun File.copyInputStreamtoFile(inputStream: InputStream){ // Получаем inputStream из ссылки не напрямую с файла,а через посредника
//       this.outputStream().use {
//           out-> inputStream.copyTo(out) // В файл копируется поток
//       }
//   }
//
//    private fun ImageRotatiton(imagefile: File): Int {
//        val rotation: Int
//        val exif = ExifInterface(imagefile.absolutePath)
//        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//        rotation = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            90
//
//        } else {
//            0
//        }
//        return rotation
//
//    }

   suspend fun ImageResize(uris: ArrayList<Uri>,act:Activity) : List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            val size = getImageSize(uris[n],act)
            val imageratio = size[0].toFloat() / size[1].toFloat()

            if (imageratio > 1) {
                if (size[0] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageratio).toInt()))
                } else {
                    tempList.add(listOf(size[0], size[1]))
                }

            } else {
                if (size[1] > MAX_IMAGE_SIZE) {
                    if (size[0] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageratio).toInt(), MAX_IMAGE_SIZE))
                    } else {
                        tempList.add(listOf(size[0], size[1]))
                    }


                }
            }

        }
       for (n in uris.indices){
           kotlin.runCatching {
               bitmapList.add(Picasso.get().load(uris[n]).resize(tempList[n][0],tempList[n][1]).get())
           }
       }

       return@withContext bitmapList

    }


   private suspend fun getBitmapFromUri(uris: List<String?>) : List<Bitmap> = withContext(Dispatchers.IO) { // Функция для получения bitmap картинок для отображение в viewpager, запуск функции на второстеменно потоке
        val bitmapList = ArrayList<Bitmap>() // Временнный список

        for (n in uris.indices){ // Берем по очереди из массива ссылки
            kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[n]).get()) // берем bitmap
            }
        }

        return@withContext bitmapList

    }
      fun FillImageArray(adPost : AdPost, adapter : ImageAdapter) { // Функция для заполнения массива с ссылками
        val listUris = listOf( // Создание списка со всеми ссылками на картинки
            adPost.mainImage,
            adPost.image2,
            adPost.image3,
            adPost.image4
        )
        CoroutineScope(Dispatchers.Main).launch { // Запуск курутины на основном потоке,где будет обновлятся адаптер после того как выполнится функция для получения bitmap на второстепенном потоке
            val bitmapList =
                getBitmapFromUri(listUris) // Когда запустится курутина, это запуститься первое, пока не достанет все битмапы
            adapter.updateAdapter(bitmapList as ArrayList<Bitmap>) // Обновляем адаптер с загруженными битмапами

        }


    }
}