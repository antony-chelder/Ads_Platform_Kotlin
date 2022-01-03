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

                val adFilter = FilterManager.createFilter(ad) 

                db.child(ad.key ?: "empty").child(FILTER_NODE) 
                    .setValue(adFilter).addOnCompleteListener {
                        finishlistener.onFinish()
                    }
            }
    }


    fun viewCount(ad: AdPost) { 
        var counter =
            ad.viewsCounter.toInt() 
        counter++ 
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(
                InfoItem(
                    counter.toString(),
                    ad.emailCounter,
                    ad.callsCounter
                )
            ) 

    }


   private fun addToFavs(
        ad: AdPost,
        listener: FinishworkListener
    ) { 
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAV_NODE).child(uid).setValue(uid)
                    .addOnCompleteListener {
                        if (it.isSuccessful) { 
                            listener.onFinish()
                        }
                    }
            }
        }

    }

   private fun removeFromFavs(
        ad: AdPost,
        listener: FinishworkListener
    ) { 
        ad.key?.let { 
            auth.uid?.let { uid ->
                db.child(it).child(FAV_NODE).child(uid).removeValue() 
                    .addOnCompleteListener {
                        if (it.isSuccessful) { 
                            listener.onFinish()
                        }
                    }
            }
        }

    }

    fun onFavClick(ad: AdPost,listener:FinishworkListener){ 
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

    fun getmyFavs(readDataCallback: ReadDataCallback?) { 
        val query = db.orderByChild("/favs/${auth.uid}")
            .equalTo(auth.uid) 
        readDataFromDatabase(query, readDataCallback)

    }

    fun getAllAdsFirstPage(filter: String,readDataCallback: ReadDataCallback?) { 
        val query = if(filter.isEmpty()){ 
            db.orderByChild(ALL_FILTER).limitToLast(ADS_LIMIT) 
        } else{
            getAdsByFilter(filter)
        }
        readDataFromDatabase(query, readDataCallback)

    }

    fun getAdsByFilter(filter : String) :Query { 
        val orderBy = filter.split("|")[0] 
        val filterData = filter.split("|")[1] 
        return db.orderByChild("/adFilter/$orderBy").startAt(filterData).endAt(filterData + "\uf8ff").limitToLast(ADS_LIMIT) 


    }

    fun getAdsByFilterCategory(category:String,filter : String) :Query { 
        val orderBy = "cat" + "_" + filter.split("|")[0] 
        val filterData =  category + "_" + filter.split("|")[1] 
        return db.orderByChild("/adFilter/$orderBy").startAt(filterData).endAt(filterData + "\uf8ff").limitToLast(ADS_LIMIT) 


    }


    fun getAdsByFilterCategoryNextPage(category:String,time: String,filter : String,readDataCallback: ReadDataCallback?) { 
        val orderBy = "cat" + "_" + filter.split("|")[0] 
        val filterData =  category + "_" + filter.split("|")[1] 
        val query = db.orderByChild(CATEGORY_FILTER).endBefore(filterData + "_$time").limitToLast(ADS_LIMIT) 
        readDataFromDatabaseNextPage(query,filterData,orderBy,readDataCallback)


    }


    fun getAllAdsNextPage(time : String,filter: String,readDataCallback: ReadDataCallback?) { 
          if(filter.isEmpty()){ 
           val query =  db.orderByChild(ALL_FILTER).endBefore(time).limitToLast(ADS_LIMIT)
              readDataFromDatabase(query, readDataCallback)
        } else{
            getAdsByFilterNextPage(filter,time,readDataCallback)
        }


    }

     private fun getAdsByFilterNextPage(filter : String,time: String,readDataCallback: ReadDataCallback?){ 
        val orderBy = filter.split("|")[0] 
        val filterData = filter.split("|")[1] 
        val query = db.orderByChild("/adFilter/$orderBy").endBefore(filterData + "_$time").limitToLast(ADS_LIMIT) 
         readDataFromDatabaseNextPage(query,filterData,orderBy,readDataCallback)


    }





    fun getCategoryAdsFirstPage(filter: String,cat : String,readDataCallback: ReadDataCallback?) { 
        val query = if(filter.isEmpty()){ 
            db.orderByChild(CATEGORY_FILTER).startAt(cat).endAt(cat + "\uf8ff").limitToLast(ADS_LIMIT)
        } else{
            getAdsByFilterCategory(cat,filter)
        }
        readDataFromDatabase(query, readDataCallback)

    }



    fun getCategoryAdsNextPage(filter: String,cat : String, time: String,readDataCallback: ReadDataCallback?) { 
         if(filter.isEmpty()){
             val query = db.orderByChild(CATEGORY_FILTER).endBefore(cat + "_$time").limitToLast(ADS_LIMIT) 
            readDataFromDatabase(query, readDataCallback)
        } else {
            getAdsByFilterCategoryNextPage(cat,time,filter,readDataCallback)

        }


    }

    fun deleteadformDb(
        ad: AdPost,
        listener: FinishworkListener
    ) { 
        if (ad.key == null && ad.uid == null) return
        db.child(ad.key!!).child(ad.uid!!).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }


    private fun readDataFromDatabase(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object :
            ValueEventListener { 
            override fun onDataChange(snapshot: DataSnapshot) {
                val adListArray = ArrayList<AdPost>()
                for (item in snapshot.children) {
                    var ad: AdPost? = null

                    item.children.forEach { 

                        if (ad == null) { 
                            ad = it.child(AD_NODE)
                                .getValue(AdPost::class.java) 
                        }
                    }

                    val infoItem = item.child(INFO_NODE)
                        .getValue(InfoItem::class.java) 

                    val favCounter = item.child(FAV_NODE).childrenCount 
                    ad?.favcounter = favCounter.toString() 
                    val isFav = auth.uid?.let { item.child(FAV_NODE).child(it).getValue(String::class.java) } 
                    ad?.isFav = isFav != null 
                    ad?.viewsCounter = infoItem?.viewsCounter
                        ?: "0" 
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null) {

                        adListArray.add(ad!!)
                    }
                    readDataCallback?.readData(adListArray) 
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun readDataFromDatabaseNextPage(query: Query,filter: String,orderBy:String,readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object :
            ValueEventListener { 
            override fun onDataChange(snapshot: DataSnapshot) {
                val adListArray = ArrayList<AdPost>()
                for (item in snapshot.children) {
                    var ad: AdPost? = null

                    item.children.forEach { 

                        if (ad == null) { 
                            ad = it.child(AD_NODE)
                                .getValue(AdPost::class.java) 
                        }
                    }

                    val infoItem = item.child(INFO_NODE)
                        .getValue(InfoItem::class.java) 

                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString() 



                    val favCounter = item.child(FAV_NODE).childrenCount 
                    ad?.favcounter = favCounter.toString() 
                    val isFav = auth.uid?.let { item.child(FAV_NODE).child(it).getValue(String::class.java) } 
                    ad?.isFav = isFav != null 
                    ad?.viewsCounter = infoItem?.viewsCounter
                        ?: "0" 
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null && filterNodeValue.startsWith(filter)) { 

                        adListArray.add(ad!!)
                    }
                    readDataCallback?.readData(adListArray) 
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
