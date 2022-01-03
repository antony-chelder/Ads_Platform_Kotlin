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
    private lateinit var adapter : ImageAdapter 
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
         viewPager2.adapter = adapter 
        }
        getIntentFromMain()
        imageChangeCounter()



    }

    private fun FillTextData() = with(binding){ 
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

    private fun CallTel(){ 
        val callUri = "tel:${adPost?.tel}" 
        val i = Intent(Intent.ACTION_DIAL) 
        i.data = callUri.toUri()
        startActivity(i)

    }

    private fun SendEmail(){
        val i = Intent(Intent.ACTION_SEND) 
        i.type = "message/rfc822" 
        i.apply { 
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adPost?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Обьявление")
            putExtra(Intent.EXTRA_SUBJECT, "Меня интересует Ваше обьявление!")
        }
        try { // Запуск блока на случай, если нету установленного приложения для открытия почти
          startActivity(Intent.createChooser(i,"Open With")) 

        }catch (e: ActivityNotFoundException){

        }
    }

    private fun IsWithSend(withsend: Boolean) : String{ 
        return if(withsend) "Yes" else "No"
    }

    private fun getIntentFromMain(){ 

        adPost = intent.getSerializableExtra(AD_KEY) as AdPost 
        ImageManager.FillImageArray(adPost!!,adapter) 
        FillTextData()

    }


    private fun imageChangeCounter(){ 
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ 
            override fun onPageSelected(position: Int) { 
                super.onPageSelected(position)
                val imagecounter = "${position + 1}/${binding.viewPager2.adapter?.itemCount}" 
                binding.imageCounter.text = imagecounter 
            }

        })

    }



    companion object{
        const val AD_KEY = "adkey"
    }
}
