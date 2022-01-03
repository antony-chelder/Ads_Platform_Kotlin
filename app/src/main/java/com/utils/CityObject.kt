package com.utils

import android.content.Context
import org.json.JSONObject
import java.io.InputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object CityObject {
    fun getAllCountries(context: Context):ArrayList<String>{
      var tempArray = ArrayList<String>()
        try {
            val inputStream:InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonfile = String(bytesArray)
            val jsonObject = JSONObject(jsonfile)
            val Countrynames = jsonObject.names()
            if(Countrynames != null) {
                for (n in 0 until Countrynames.length()) {
                    tempArray.add(Countrynames.getString(n))

                }
            }

        } catch (e:Exception){

        }
        return tempArray
    }
    fun getAllCities(country:String,context: Context):ArrayList<String>{
        var tempArray = ArrayList<String>()
        try {
            val inputStream:InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonfile = String(bytesArray)
            val jsonObject = JSONObject(jsonfile)
            val citynames = jsonObject.getJSONArray(country)

                for (n in 0 until citynames.length()) {
                    tempArray.add(citynames.getString(n))


            }

        } catch (e:Exception){

        }
        return tempArray
    }
    fun filterListData(list:ArrayList<String>,searchText:String?):ArrayList<String>{
        val tempList = ArrayList<String>()
        if(searchText == null){
            tempList.add("No Result")
            return tempList
        }
        for(selection:String in list){
            if(selection.toLowerCase(Locale.ROOT).startsWith(searchText.toLowerCase(Locale.ROOT)))
                tempList.add(selection)
        }
        if(tempList.isEmpty())tempList.add("No Result")
        return tempList


    }

}