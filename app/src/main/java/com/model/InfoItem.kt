package com.model

data class InfoItem( // Класс который будет хранить в себе информацию о количестве просмотров, сообщений, звонков
    val  viewsCounter : String? = null, // Указывем что по умолчанию значение может быть null
    val  emailsCounter : String? = null, // Указывем что по умолчанию значение может быть null
    val  callsCounter : String? = null // Указывем что по умолчанию значение может быть null
)
