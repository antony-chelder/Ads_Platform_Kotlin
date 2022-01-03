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
                putExtra(FILTER_KEY,createFilter()) // Отправка данных с фильтра на MainActivity
            }
            setResult(RESULT_OK,i) // Возвращение результата
            finish()

        }

    }


    private fun onClickClear() = with(binding){ // Функция для очистки фильтра
        bClear.setOnClickListener {
           selectCountry.text = getString(R.string.selectcountry)
           selectCity.text = getString(R.string.selectсity)
            edIndex.setText("")
            checkSend.isChecked = false
            setResult(RESULT_CANCELED) // Как только очищается фильтр, тогда и примененные данные для фильтра также очищаются, передаем этот код на MainActivity
        }

    }

    private fun getFilterResult() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty"){
          val filterArray = filter.split("_") // Будут выдаватся элементы по нижнему подчеркиванию, которые пришли с activity
            if(filterArray[0] != "empty") selectCountry.text = filterArray[0] // Заполнение данными которые уже были применены в фильтре если они есть
            if(filterArray[1] != "empty") selectCity.text = filterArray[1]
            if(filterArray[2] != "empty") edIndex.setText(filterArray[2])
            checkSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private  fun createFilter(): String = with(binding){ // Функция будет возвращать сформированный стринг для фильтрации
        val sBilder = StringBuilder() // Создание builder для формирование одной строки всех данных
        val arrayTempFilter = listOf(selectCountry.text,
         selectCity.text,
         edIndex.text,
         checkSend.isChecked.toString()) // Формирование массива со всеми указанными данными

        for ((index,dataFilterS) in arrayTempFilter.withIndex()){ // Цикл прогоняет все данные в списке, где будут проходить необходимые проверки
              if(dataFilterS != getString(R.string.selectcountry) && dataFilterS != getString(R.string.selectсity) && dataFilterS.isNotEmpty() ) { // Проверка, чтобы страна, город были выбраны, также индекс не пуст, иначе не добавлять в сформированный стринг
                sBilder.append(dataFilterS) // После проверки на условия, добавляются данные в билдер
                 if(index != arrayTempFilter.size - 1) sBilder.append("_") // Если это не последний элемент, то добавляется подчеркивание между данными
              } else {
                  sBilder.append("empty") // Если пустые элементы, то передаем слово empty, чтобы легче потом передавать
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

    private fun HomeActionBarSettings(){ // Активация кнопки назад
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}