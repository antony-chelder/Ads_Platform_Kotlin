package com.model

import java.io.Serializable

data class AdPost(
        val country : String? = null,
        val city : String? = null,
        val tel : String? = null,
        val index : String? = null,
        val email : String? = null,
        val withsend : String? = null,
        val category : String? = null,
        val price : String? = null,
        val mainImage : String? = null,
        val image2 : String? = null,
        val image3 : String? = null,
        val image4 : String? = null,
        var isFav : Boolean = false,
        val description : String? = null,
        val key : String? = null,
        var favcounter: String = "0",
        val title : String? = null,
        val uid : String? = null,
        val time : String = "0",

        
        var viewsCounter : String = "0",
        var emailCounter : String = "0",
        var callsCounter : String = "0"

) : Serializable
