package com.model

import com.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth
    val storage = Firebase.storage.getReference(MAIN_NODE)

    fun publishAd(ad: AdPost, finishlistener: FinishworkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty").child(auth.uid!!).child(AD_NODE)
            .setValue(ad).addOnCompleteListener {

                val adFilter = FilterManager.createFilter(ad) // Создается шаблон как будут хранится данные в узле

                db.child(ad.key ?: "empty").child(FILTER_NODE) // Создаем в ключе обьявления еще путь для фильтрации
                    .setValue(adFilter).addOnCompleteListener {
                        finishlistener.onFinish()
                    }
            }
    }


    fun viewCount(ad: AdPost) { // Функция для считывания просмотров при просмотре обьявления, передаем наш класс AdPost по нему будем добератся до ключа
        var counter =
            ad.viewsCounter.toInt() // Создаем счетчик с данных из класса, первращаем в Int, чтобы увеличивать на 1
        counter++ // Увеличение на 1
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(
                InfoItem(
                    counter.toString(),
                    ad.emailCounter,
                    ad.callsCounter
                )
            ) // Записываем текущее значение счетчика, передаем целый класс с данными

    }


   private fun addToFavs(
        ad: AdPost,
        listener: FinishworkListener
    ) { // Функция для добавление в избранное, добавляем интерфейс,чтобы добавление срабатывало при успешной операции
        ad.key?.let { // Прописывается путь куда будет добавлятся, в блоке let, так как кеу может быть null и пропустит код дальше если уже есть ключ
            auth.uid?.let { uid ->
                db.child(it).child(FAV_NODE).child(uid).setValue(uid)
                    .addOnCompleteListener {// Добавляется наш uid по внутри узла favs, также проверка на успешность выполнения с помощью listener
                        if (it.isSuccessful) { // Запускаем наш listener когда успешно добавлено в избранное
                            listener.onFinish()
                        }
                    }
            }
        }

    }

   private fun removeFromFavs(
        ad: AdPost,
        listener: FinishworkListener
    ) { // Функция для добавление в избранное, добавляем интерфейс,чтобы добавление срабатывало при успешной операции
        ad.key?.let { // Прописывается путь куда будет добавлятся, в блоке let, так как кеу может быть null и пропустит код дальше если уже есть ключ
            auth.uid?.let { uid ->
                db.child(it).child(FAV_NODE).child(uid).removeValue() // Убираем с избранного
                    .addOnCompleteListener {// Добавляется наш uid по внутри узла favs, также проверка на успешность выполнения с помощью listener
                        if (it.isSuccessful) { // Запускаем наш listener когда успешно добавлено в избранное
                            listener.onFinish()
                        }
                    }
            }
        }

    }

    fun onFavClick(ad: AdPost,listener:FinishworkListener){ // Проверка какую функцию нам нужно запустить, если оно находится в избранном или нет
        if (ad.isFav){
            removeFromFavs(ad,listener)
        }else {
            addToFavs(ad,listener)
        }

    }

    fun getmyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDatabase(query, readDataCallback)

    }

    fun getmyFavs(readDataCallback: ReadDataCallback?) { // Функция для избранного
        val query = db.orderByChild("/favs/${auth.uid}")
            .equalTo(auth.uid) // Путь по котрому будет считыватся, где на пути в favs id будет равно личному id
        readDataFromDatabase(query, readDataCallback)

    }

    fun getAllAdsFirstPage(filter: String,readDataCallback: ReadDataCallback?) { // Функция для загрузки первой страницы, просто берет последние порции обьявлений
        val query = if(filter.isEmpty()){ // Проверка, если фильтр пуст, то запускается один запрос, если нет, то другой
            db.orderByChild(ALL_FILTER).limitToLast(ADS_LIMIT) // Берет по порциям, не включительно последнее обьявление по времени от 0
        } else{
            getAdsByFilter(filter)
        }
        readDataFromDatabase(query, readDataCallback)

    }

    fun getAdsByFilter(filter : String) :Query { // Функция для того чтобы брать обьявления из базы данных по категории и времени
        val orderBy = filter.split("|")[0] // Разделение полного фильтра, по символу, данные выбранные в фильтре, по которому нужно будет фильтровать
        val filterData = filter.split("|")[1] // Разделение полного фильтра, по символу, чтобы достать от туда путь для запроса
        return db.orderByChild("/adFilter/$orderBy").startAt(filterData).endAt(filterData + "\uf8ff").limitToLast(ADS_LIMIT) // Берет по порциям, в категории которую мы передаем, отсортируется во время возростания по времени, также передается фильтр данных по которому искать


    }

    fun getAdsByFilterCategory(category:String,filter : String) :Query { // Функция для того чтобы брать обьявления из базы данных по категории и времени
        val orderBy = "cat" + "_" + filter.split("|")[0] // Разделение полного фильтра, по символу, данные выбранные в фильтре, по которому нужно будет фильтровать, впереди будет идти выбранная категория
        val filterData =  category + "_" + filter.split("|")[1] // Разделение полного фильтра, по символу, чтобы достать от туда путь для запроса
        return db.orderByChild("/adFilter/$orderBy").startAt(filterData).endAt(filterData + "\uf8ff").limitToLast(ADS_LIMIT) // Берет по порциям, в категории которую мы передаем, отсортируется во время возростания по времени, также передается фильтр данных по которому искать


    }


    fun getAdsByFilterCategoryNextPage(category:String,time: String,filter : String,readDataCallback: ReadDataCallback?) { // Функция для того чтобы брать обьявления из базы данных по категории и времени
        val orderBy = "cat" + "_" + filter.split("|")[0] // Разделение полного фильтра, по символу, данные выбранные в фильтре, по которому нужно будет фильтровать, впереди будет идти выбранная категория
        val filterData =  category + "_" + filter.split("|")[1] // Разделение полного фильтра, по символу, чтобы достать от туда путь для запроса
        val query = db.orderByChild(CATEGORY_FILTER).endBefore(filterData + "_$time").limitToLast(ADS_LIMIT) // Берет по порциям, в категории которую мы передаем, отсортируется во время возростания по времени, также передается фильтр данных по которому искать
        readDataFromDatabaseNextPage(query,filterData,orderBy,readDataCallback)


    }


    fun getAllAdsNextPage(time : String,filter: String,readDataCallback: ReadDataCallback?) { // Передача lastTime, чтобы отслеживать время обьявлений, функция работает во время скролла, берет следущие от последнего времени
          if(filter.isEmpty()){ // Берет по порциям, не включительно последнее обьявление по времени от 0
           val query =  db.orderByChild(ALL_FILTER).endBefore(time).limitToLast(ADS_LIMIT)
              readDataFromDatabase(query, readDataCallback)
        } else{
            getAdsByFilterNextPage(filter,time,readDataCallback)
        }


    }

     private fun getAdsByFilterNextPage(filter : String,time: String,readDataCallback: ReadDataCallback?){ // Функция для того чтобы подгружать следущие  обьявления из базы данных по категории и времени
        val orderBy = filter.split("|")[0] // Разделение полного фильтра, по символу, данные выбранные в фильтре, по которому нужно будет фильтровать
        val filterData = filter.split("|")[1] // Разделение полного фильтра, по символу, чтобы достать от туда путь для запроса
        val query = db.orderByChild("/adFilter/$orderBy").endBefore(filterData + "_$time").limitToLast(ADS_LIMIT) // Берет по порциям, в категории которую мы передаем, отсортируется во время возростания по времени, также передается фильтр данных по которому искать
         readDataFromDatabaseNextPage(query,filterData,orderBy,readDataCallback)


    }





    fun getCategoryAdsFirstPage(filter: String,cat : String,readDataCallback: ReadDataCallback?) { // Функция для того чтобы брать обьявления из базы данных по категории и времени
        val query = if(filter.isEmpty()){ // Берет по порциям, в категории которую мы передаем, отсортируется во время возростания по времени
            db.orderByChild(CATEGORY_FILTER).startAt(cat).endAt(cat + "\uf8ff").limitToLast(ADS_LIMIT)
        } else{
            getAdsByFilterCategory(cat,filter)
        }
        readDataFromDatabase(query, readDataCallback)

    }



    fun getCategoryAdsNextPage(filter: String,cat : String, time: String,readDataCallback: ReadDataCallback?) { // Функция для того чтобы брать обьявления из базы данных по категории и времени, для подгружения разделяем отдельно cat, time так как фильтр идет между ними
         if(filter.isEmpty()){
             val query = db.orderByChild(CATEGORY_FILTER).endBefore(cat + "_$time").limitToLast(ADS_LIMIT) // Берет по порциям, не включительно последнее обьявление по времени от 0
            readDataFromDatabase(query, readDataCallback)
        } else {
            getAdsByFilterCategoryNextPage(cat,time,filter,readDataCallback)

        }


    }

    fun deleteadformDb(
        ad: AdPost,
        listener: FinishworkListener
    ) { // Передаем listener, так как это трудоемкая операция, будет ожидатся как только удалится, тогда будет обновлятся адаптер
        if (ad.key == null && ad.uid == null) return
        db.child(ad.key!!).child(ad.uid!!).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }


    private fun readDataFromDatabase(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object :
            ValueEventListener { // Слушатель который считывает один раз с базы данных
            override fun onDataChange(snapshot: DataSnapshot) {
                val adListArray = ArrayList<AdPost>()
                for (item in snapshot.children) {
                    var ad: AdPost? = null

                    item.children.forEach { // Пробегаем через цикл в узлах внутри ключей

                        if (ad == null) { // Проверка, на случай если второй путь info будет впереди ключа, и в случае если ad == null, то запустится еще списаок и выберет нужный узел
                            ad = it.child(AD_NODE)
                                .getValue(AdPost::class.java) // Как только находит нужный путь, и так как уже нашли ad, если info путь, то не сработает, потому что id у всех разный
                        }
                    }

                    val infoItem = item.child(INFO_NODE)
                        .getValue(InfoItem::class.java) // Считывание другого узла InfoItem где хранятся данные о количестве просмотров,звонков и ткд

                    val favCounter = item.child(FAV_NODE).childrenCount // Подсчитываем количество избранных в узле favs
                    ad?.favcounter = favCounter.toString() // Записали счетчик избранных
                    val isFav = auth.uid?.let { item.child(FAV_NODE).child(it).getValue(String::class.java) } // Берем id в fav, добавлено в избранное или нет
                    ad?.isFav = isFav != null // Если не равно null значит обьявление находится в избранном и isFav = true
                    ad?.viewsCounter = infoItem?.viewsCounter
                        ?: "0" // Перезагрузили информацию в класс Ad, проверка если значения не пришли, то присваеваем значение по дэфолту 0
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null) {

                        adListArray.add(ad!!)
                    }
                    readDataCallback?.readData(adListArray) // Передаем полученный список на адаптер, через коллбэк
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun readDataFromDatabaseNextPage(query: Query,filter: String,orderBy:String,readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object :
            ValueEventListener { // Слушатель который считывает один раз с базы данных
            override fun onDataChange(snapshot: DataSnapshot) {
                val adListArray = ArrayList<AdPost>()
                for (item in snapshot.children) {
                    var ad: AdPost? = null

                    item.children.forEach { // Пробегаем через цикл в узлах внутри ключей

                        if (ad == null) { // Проверка, на случай если второй путь info будет впереди ключа, и в случае если ad == null, то запустится еще списаок и выберет нужный узел
                            ad = it.child(AD_NODE)
                                .getValue(AdPost::class.java) // Как только находит нужный путь, и так как уже нашли ad, если info путь, то не сработает, потому что id у всех разный
                        }
                    }

                    val infoItem = item.child(INFO_NODE)
                        .getValue(InfoItem::class.java) // Считывание другого узла InfoItem где хранятся данные о количестве просмотров,звонков и ткд

                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString() // Считывание конкретного фильтра (его значения) по которому фильтруем



                    val favCounter = item.child(FAV_NODE).childrenCount // Подсчитываем количество избранных в узле favs
                    ad?.favcounter = favCounter.toString() // Записали счетчик избранных
                    val isFav = auth.uid?.let { item.child(FAV_NODE).child(it).getValue(String::class.java) } // Берем id в fav, добавлено в избранное или нет
                    ad?.isFav = isFav != null // Если не равно null значит обьявление находится в избранном и isFav = true
                    ad?.viewsCounter = infoItem?.viewsCounter
                        ?: "0" // Перезагрузили информацию в класс Ad, проверка если значения не пришли, то присваеваем значение по дэфолту 0
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null && filterNodeValue.startsWith(filter)) { // Идет проверка на то, чтобы путь считывания по фильтру, совпадал с выбранным фильтром и подгружать только нужные обьявления

                        adListArray.add(ad!!)
                    }
                    readDataCallback?.readData(adListArray) // Передаем полученный список на адаптер, через коллбэк
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    companion object {
        const val AD_NODE = "ad"
        const val MAIN_NODE = "main"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val ALL_FILTER = "/adFilter/time"
        const val CATEGORY_FILTER = "/adFilter/cat_time"
        const val FAV_NODE = "favs"
        const val ADS_LIMIT = 2
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<AdPost>)
    }

    interface FinishworkListener {
        fun onFinish()
    }
}