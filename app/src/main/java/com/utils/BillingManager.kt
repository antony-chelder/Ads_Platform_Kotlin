package com.utils

import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class BillingManager(val act : AppCompatActivity) {
    private var billingClient : BillingClient? = null // Инстанция BillingClient через который будут реализововатся покупки


    init {
     setAppBillingClient()
    }

    private fun setAppBillingClient(){ // Функция для настройки BillingClient
        billingClient = BillingClient.newBuilder(act).setListener(getPurchaseListener()).enablePendingPurchases().build() // Настройка BillingClient
    }


     fun startConnection(){ // Функция которая запускается когда совершается покупка
         billingClient?.startConnection(object : BillingClientStateListener{
             override fun onBillingServiceDisconnected() {

             }

             override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
             }

         })
    }


    private fun getItem(){ // Создаем продукт и запускаем диалог с Плеймаркете, с информацией о покупке
        val pokupList = ArrayList<String>() // Создача списка покупок, хоть и одно, но все равно создается как массив
        pokupList.add(REMOVE_ADS) // Добавляем все покупки которые есть
        val pokupDetails = SkuDetailsParams.newBuilder() // Детали покупки,информация о покупки
        pokupDetails.setSkusList(pokupList).setType(BillingClient.SkuType.INAPP) // Сформировали полную информацию о покупке, указали тип встроенные покупки
        billingClient?.querySkuDetailsAsync(pokupDetails.build()){ // Создали запрос
                result,list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { // Проверка, если все прошло успешно
                    if(!list.isNullOrEmpty()){ // Проверка если все прошло успешно, то создается Launcher( диалог с покупкой)
                      val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(list[0]).build() // Подготовка, что есть такая покупка на плеймаркете и все готово
                        billingClient?.launchBillingFlow(act,billingFlowParams) // Запуск launcher(диалога) с покупкой
                    }
                }
            }

        }// Отправка данные асинхронно, не останавливая основной поток


    }

    private fun getPurchaseListener() : PurchasesUpdatedListener{ // Listener который прослушивает, что происходит с покупками
     return PurchasesUpdatedListener{
         result,list ->
         run {
             if (result.responseCode == BillingClient.BillingResponseCode.OK) { // Проверка, если все прошло успешно
              list?.get(0).let { // Берем с 0 позиции так как будет 1 покупка

              }
             }
         }
     }
    }

    companion object{
        const val REMOVE_ADS = "remove_ads"
    }
}