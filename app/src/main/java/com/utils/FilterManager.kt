package com.utils

import com.model.AdPost
import com.model.AdPostFilter
import java.lang.StringBuilder

object FilterManager {

    fun createFilter(ad: AdPost) : AdPostFilter{
        return AdPostFilter( 
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withsend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.index}_${ad.withsend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withsend}_${ad.time}",
            "${ad.category}_${ad.withsend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withsend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.index}_${ad.withsend}_${ad.time}",
            "${ad.country}_${ad.index}_${ad.withsend}_${ad.time}",
            "${ad.country}_${ad.withsend}_${ad.time}",
            "${ad.withsend}_${ad.time}",
            "${ad.index}_${ad.withsend}_${ad.time}",

        )
    }

    fun getFilter(filter : String) : String{ 
        val sBuilderNode = StringBuilder() 
        val sBuilderData = StringBuilder() 
        val tempArray = filter.split("_")

        if(tempArray[0] != "empty") { 
            sBuilderNode.append("country_")
            sBuilderData.append("${tempArray[0]}_") 
        }
        if(tempArray[1] != "empty"){
            sBuilderNode.append("city_")
            sBuilderData.append("${tempArray[1]}_")

        }
        if(tempArray[2] != "empty"){
            sBuilderNode.append("index_")
            sBuilderData.append("${tempArray[2]}_")
        }

            sBuilderData.append(tempArray[3])
            sBuilderNode.append("withsend_time")


        return "$sBuilderNode|$sBuilderData"
    }
}
