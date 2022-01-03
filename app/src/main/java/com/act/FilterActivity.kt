package com.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.utils.CityObject
import com.dialogs_list.DialogSpinnerHelper
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.ActivityFilterBinding
import java.lang.StringBuilder

class FilterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OnclickSelectCountry()
        OnclickSelectCity()
        onClickDone()
        onClickClear()
        getFilterResult()
        HomeActionBarSettings()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun OnclickSelectCountry() = with(binding) {
        selectCountry.setOnClickListener {
            val listcountries = CityObject.getAllCountries(this@FilterActivity)
            if (selectCity.text.toString() != getString(R.string.selectсity)) {
                selectCity.text = getString(R.string.selectсity)
            }
            dialog.showspinnerdialog(this@FilterActivity, listcountries, binding.selectCountry)
        }

    }

    private fun onClickDone() = with(binding){
        bDone.setOnClickListener {
            val i = Intent().apply {
                putExtra(FILTER_KEY,createFilter()) 
            }
            setResult(RESULT_OK,i) 
            finish()

        }

    }


    private fun onClickClear() = with(binding){ 
        bClear.setOnClickListener {
           selectCountry.text = getString(R.string.selectcountry)
           selectCity.text = getString(R.string.selectсity)
            edIndex.setText("")
            checkSend.isChecked = false
            setResult(RESULT_CANCELED) 
        }

    }

    private fun getFilterResult() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty"){
          val filterArray = filter.split("_") 
            if(filterArray[0] != "empty") selectCountry.text = filterArray[0]
            if(filterArray[1] != "empty") selectCity.text = filterArray[1]
            if(filterArray[2] != "empty") edIndex.setText(filterArray[2])
            checkSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private  fun createFilter(): String = with(binding){ 
        val sBilder = StringBuilder() 
        val arrayTempFilter = listOf(selectCountry.text,
         selectCity.text,
         edIndex.text,
         checkSend.isChecked.toString()) 

        for ((index,dataFilterS) in arrayTempFilter.withIndex()){ 
              if(dataFilterS != getString(R.string.selectcountry) && dataFilterS != getString(R.string.selectсity) && dataFilterS.isNotEmpty() ) { 
                sBilder.append(dataFilterS)
                 if(index != arrayTempFilter.size - 1) sBilder.append("_") 
              } else {
                  sBilder.append("empty") 
                  if(index != arrayTempFilter.size - 1) sBilder.append("_")
              }


        }

        return sBilder.toString()



    }


   private fun OnclickSelectCity() = with(binding) {
       selectCity.setOnClickListener {
           val selectedCountry = selectCountry.text.toString()
           val listcity = CityObject.getAllCities(selectedCountry, this@FilterActivity)
           if (selectedCountry != getString(R.string.selectcountry)) {
               dialog.showspinnerdialog(this@FilterActivity, listcity,selectCity)
           } else {
               Toast.makeText(this@FilterActivity, "No country selected", Toast.LENGTH_LONG).show()
           }
       }


    }

    private fun HomeActionBarSettings(){ 
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}
