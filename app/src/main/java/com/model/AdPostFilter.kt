package com.model

data class AdPostFilter( // Класс где будет фильтрация по времени и категориям
    val time : String? = null,
    val cat_time : String? = null,

    // Всевозможно фильтрация, чтобы брать с определенной категории
    val cat_country_city_index_withsend_time : String? = null,
    val cat_country_index_withsend_time : String? = null,
    val cat_country_withsend_time : String? = null,
    val cat_withsend_time : String? = null,
    val cat_index_withsend_time : String? = null,


    // Всевозможно фильтрация, без определенной категории
    val country_city_index_withsend_time : String? = null,
    val country_index_withsend_time : String? = null,
    val country_withsend_time : String? = null,
    val withsend_time : String? = null,
    val index_withsend_time : String? = null,

    )
