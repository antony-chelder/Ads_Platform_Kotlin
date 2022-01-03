package com.utils

import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*

class BillingManager(val act : AppCompatActivity) {
    private var billingClient : BillingClient? = null 


    init {
     setAppBillingClient()
    }

    private fun setAppBillingClient(){ 
        billingClient = BillingClient.newBuilder(act).setListener(getPurchaseListener()).enablePendingPurchases().build() 
    }


     fun startConnection(){ 
         billingClient?.startConnection(object : BillingClientStateListener{
             override fun onBillingServiceDisconnected() {

             }

             override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
             }

         })
    }


    private fun getItem(){ 
        val pokupList = ArrayList<String>() 
        pokupList.add(REMOVE_ADS) 
        val pokupDetails = SkuDetailsParams.newBuilder() 
        pokupDetails.setSkusList(pokupList).setType(BillingClient.SkuType.INAPP) 
        billingClient?.querySkuDetailsAsync(pokupDetails.build()){ 
                result,list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) { 
                    if(!list.isNullOrEmpty()){ 
                      val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(list[0]).build() 
                        billingClient?.launchBillingFlow(act,billingFlowParams) 
                    }
                }
            }

        }

    }

    private fun getPurchaseListener() : PurchasesUpdatedListener{ 
     return PurchasesUpdatedListener{
         result,list ->
         run {
             if (result.responseCode == BillingClient.BillingResponseCode.OK) { 
              list?.get(0).let { 

              }
             }
         }
     }
    }

    companion object{
        const val REMOVE_ADS = "remove_ads"
    }
}
