package com.act

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.utils.ImageManager
import com.adapter.ImageAdapter
import com.model.AdPost
import com.tony_fire.descorderkotlin.databinding.ActivityDescBinding

class DescActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDescBinding
    private lateinit var adapter : ImageAdapter // Инстанция адаптера для картинок
    private var adPost : AdPost? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.bCall.setOnClickListener {
         CallTel()
        }

        binding.bEmail.setOnClickListener {
         SendEmail()
        }
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
         viewPager2.adapter = adapter // Подключили адаптер в ViewPager
        }
        getIntentFromMain()
        imageChangeCounter()



    }

    private fun FillTextData() = with(binding){ // Заполнение текстовой записи
        if(adPost != null) {
            tvTitle.text = adPost!!.title
            tvDesc.text = adPost!!.description
            tvPrice.text = adPost!!.price
            tvTel.text = adPost!!.tel
            tvEmail.text = adPost!!.email
            tvCountry.text = adPost!!.country
            tvCity.text = adPost!!.city
            tvIndex.text = adPost!!.index
            tvCategory.text = adPost!!.category
            tvWithsend.text = IsWithSend(adPost!!.withsend.toBoolean())
        }
    }

    private fun CallTel(){ // Функция для звонка
        val callUri = "tel:${adPost?.tel}" // Создается ури для отправки в приложение звонка на телефоне
        val i = Intent(Intent.ACTION_DIAL) // Intent для открытия приложения для звонков
        i.data = callUri.toUri() // Передача данных
        startActivity(i)

    }

    private fun SendEmail(){
        val i = Intent(Intent.ACTION_SEND) // Отправка в манифест информации о том что мы будет открывать приложение для почты
        i.type = "message/rfc822" // Тип для сообщения
        i.apply { // Передаем в какие поля в сообщении будут заполнены
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adPost?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Обьявление")
            putExtra(Intent.EXTRA_SUBJECT, "Меня интересует Ваше обьявление!")
        }
        try { // Запуск блока на случай, если нету установленного приложения для открытия почти
          startActivity(Intent.createChooser(i,"Open With")) // Создание chooser чтобы выбралось приложение для открытия email

        }catch (e: ActivityNotFoundException){

        }
    }

    private fun IsWithSend(withsend: Boolean) : String{ // Функция чтобы выводить текст в случае true или false
        return if(withsend) "Yes" else "No"
    }

    private fun getIntentFromMain(){ // Получаем данные с MainAct так как там загружаются обьвления и при нажатии передается весь класс AdPost

        adPost = intent.getSerializableExtra(AD_KEY) as AdPost // Получили весь класс с помощью Serializable
        ImageManager.FillImageArray(adPost!!,adapter) // Достаем ссылки для картинок,заполняем их, также обновляем адаптер
        FillTextData()

    }


    private fun imageChangeCounter(){ // Счетчик картинок, при перелистовании
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ // Создан колбэк которые прослушивает перелистование в ViewPager
            override fun onPageSelected(position: Int) { // Показыввает позицию где мы остановились в перелистовании
                super.onPageSelected(position)
                val imagecounter = "${position + 1}/${binding.viewPager2.adapter?.itemCount}" // Увеличение количества значения на + 1 так как позиция идет с 0, также подсчет количества фото в адаптере
                binding.imageCounter.text = imagecounter // Передача созданного значение в текст
            }

        })

    }



    companion object{
        const val AD_KEY = "adkey"
    }
}