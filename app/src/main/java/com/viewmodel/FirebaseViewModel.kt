package com.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.model.AdPost
import com.model.DbManager

class FirebaseViewModel : ViewModel() {
    private  val dbmanager = DbManager()
    val liveadsdata = MutableLiveData<ArrayList<AdPost>>()


     fun loadAllAdsFirstPage(filter:String,){
        dbmanager.getAllAdsFirstPage(filter,object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })
    }

    fun loadAllAdsNextPage(time : String, filter: String){
        dbmanager.getAllAdsNextPage(time,filter,object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })
    }


    fun loadCatAdsFirstPage(filter: String,cat : String){
        dbmanager.getCategoryAdsFirstPage(filter,cat, object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })
    }

    fun loadCatAdsNextPage(cat: String,time: String,filter: String){
        dbmanager.getCategoryAdsNextPage(filter,cat,time, object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })
    }




    fun loadMyAds(){
        dbmanager.getmyAds( object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })
    }

    fun loadFavAds(){ 
        dbmanager.getmyFavs(object : DbManager.ReadDataCallback{ 
            override fun readData(list: ArrayList<AdPost>) {
                liveadsdata.value = list
            }

        })

        }

    fun viewCount(ad: AdPost){ 
        dbmanager.viewCount(ad)
    }

    fun onFavClick(ad: AdPost){
        dbmanager.onFavClick(ad,object : DbManager.FinishworkListener{
            override fun onFinish() {
                val updatedlist = liveadsdata.value 
                val pos = updatedlist?.indexOf(ad) 
                if( pos != -1){ 
                    pos?.let{
                        val favCounter = if(ad.isFav){ 
                            ad.favcounter.toInt() -1
                        } else{
                            ad.favcounter.toInt() +1
                        }
                        updatedlist[pos] = updatedlist[pos].copy(isFav = !ad.isFav, favcounter = favCounter.toString()) 
                    }

                }
                liveadsdata.postValue(updatedlist)
            }

        })
    }


    fun DeleteItem(ad: AdPost){
        dbmanager.deleteadformDb(ad, object: DbManager.FinishworkListener{
            override fun onFinish() {
                val updatedlist = liveadsdata.value
                updatedlist?.remove(ad)
                liveadsdata.postValue(updatedlist)
            }

        })
    }
}
