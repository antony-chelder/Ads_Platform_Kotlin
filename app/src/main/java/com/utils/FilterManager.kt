package com.utils

import com.model.AdPost
import com.model.AdPostFilter
import java.lang.StringBuilder

object FilterManager {

    fun createFilter(ad: AdPost) : AdPostFilter{
        return AdPostFilter( // Сборка запросов фильтра, помещая все данные
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

    fun getFilter(filter : String) : String{ // Функция котороя будет брать фильтер, который был выбран и будет отправляться в dbManager
        val sBuilderNode = StringBuilder() // Билдер для формирования самого узла, название параметров
        val sBuilderData = StringBuilder() // Билдер для формирования данных внутри узла
        val tempArray = filter.split("_")

        if(tempArray[0] != "empty") { // Проверка что там не empty по позициям в самом фильтре
            sBuilderNode.append("country_")
            sBuilderData.append("${tempArray[0]}_") // Берем определенные данные
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