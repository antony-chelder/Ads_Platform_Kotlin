package com.tony_fire.descorderkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Accaunt_helper.AccauntHelper
import com.utils.FilterManager
import com.act.EditAdsActivity
import com.act.FilterActivity
import com.adapter.RecycleViewAdapter
import com.dialogs.DialogConst
import com.dialogs.Dialog_helper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.model.AdPost
import com.squareup.picasso.Picasso
import com.tony_fire.descorderkotlin.databinding.ActivityMainBinding
import com.viewmodel.FirebaseViewModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, RecycleViewAdapter.AdItemListener {
    private lateinit var tvAccaunt: TextView
    private lateinit var imAccaunt: ImageView // Инстанция картинки, чтобы была возможность добратся до нее в nav_header
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = Dialog_helper(this)
    private var currentCategory :String? = null
    private var filter :String = "empty" // Глобальная переменная для фильтра
    private var filterDb :String = "" // Фильтр для базы данных, чтобы проеврять пустой он или нет
    private var clearUpdate : Boolean = true // Boolean, чтобы проверять когда нужно очищать адаптер и загружать с первой страницы или подгружать следущие
    lateinit var googleSignLauncher : ActivityResultLauncher<Intent>
    lateinit var filterLauncher : ActivityResultLauncher<Intent> // Будем ждать результата от выбранного фильтра
    val mAuth = Firebase.auth
    val adapter = RecycleViewAdapter(this)
    private val firebaseviewmodel : FirebaseViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        (application as AppMainState).showAdIfAvailable(this, object : AppMainState.OnShowAdCompleteListener{ // Добавление рекламы при запуске приложения
            override fun onShowAdComplete() {
                Log.d("Ad", "All fine")
            }

        })


        adMobInit()
        init()
        initRcView()
        initViewModel()
        navButtonClick()
        onActivityResultFilter()
        ScrollListener()


    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_bottom_all_ads
        binding.mainContent.adBanner.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mainContent.adBanner.destroy()
    }

    override fun onPause() {
        super.onPause()
        binding.mainContent.adBanner.pause()

    }


    private  fun initViewModel(){

        firebaseviewmodel.liveadsdata.observe(this,{
            val list = getAdsByCategory(it) // Передаем наш обработанный список
            if(!clearUpdate) { // Если false то подгружаем следущие , если true  то очищаем и заного загружаем
                adapter.updateAdapter(list)
            }else {
                adapter.updateWithClearAdapter(list)
            }
            binding.mainContent.tvEmpty.visibility = if(adapter.itemCount == 0){ // Проверка на не пуст ли наш список, если да, то показываем текст Пусто
                View.VISIBLE
            } else{
                View.GONE
            }
        })
    }

    private fun adMobInit() = with(binding) {
        MobileAds.initialize(this@MainActivity)
        val adRequest = AdRequest.Builder().build()
        mainContent.adBanner.loadAd(adRequest)
    }

    private fun getAdsByCategory(list : ArrayList<AdPost>) : ArrayList<AdPost>{ // Функция которая будет переворачивать элементы в правильно порядке, также сортировка четко по категориям
        val tempList = ArrayList<AdPost>()
        tempList.addAll(list) // Загрузка всех обьявлений во временный список

        if(currentCategory != getString(R.string.other_ads)){ // Проверка, если эта категория не разное, а одна из других
            tempList.clear() // Очистка списка
            list.forEach{ // Берем каждый элемент из списка
                if (currentCategory == it.category){ // Проверка если текущая категория, совпадает с категорией выбраной при создании обьявления
                    tempList.add(it) // Добавили в пустой список отсортированые элементы
                }
            }
        }
        tempList.reverse() // Переворот списка
        return tempList

    }

    private fun onActivityResultFilter(){
        filterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
          if(it.resultCode == RESULT_OK){
              filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
//              Log.d("MyLog" , "Data : $filter")
//              Log.d("MyLog" , "DataFilter : ${FilterManager.getFilter(filter)}")
              filterDb = FilterManager.getFilter(filter)
          } else if(it.resultCode == RESULT_CANCELED) {
              filterDb = ""
              filter = "empty"
          }
        }
    }

    private fun initRcView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
            mainContent.rcView.setHasFixedSize(true)

        }
    }

    fun init() {
        currentCategory = getString(R.string.other_ads)
        setSupportActionBar(binding.mainContent.toolbar)
        NavViewSettings()
        onActivityResult() // Запуск зарегестрированного лунчера
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open_toggle,
            R.string.close_toggle
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccaunt = binding.navView.getHeaderView(0).findViewById(R.id.nav_text_accaunt)
        imAccaunt = binding.navView.getHeaderView(0).findViewById(R.id.imAcc)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Создали меню
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }



    private fun navButtonClick()= with(binding){
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true // При нажатии на кнопку home список будет очищатся и заного подгружатся
            when(item.itemId){
                R.id.id_botton_add_ad -> {
                    val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(i)

                }
                R.id.id_botton_myfav ->{
                    firebaseviewmodel.loadFavAds()
                    mainContent.toolbar.title = getString(R.string.fav_ads)
                }
                R.id.id_bottom_all_ads ->{
                    currentCategory = getString(R.string.other_ads)
                    firebaseviewmodel.loadAllAdsFirstPage(filterDb)
                    mainContent.toolbar.title = getString(R.string.other_ads)
                }
                R.id.id_botton_my_ads ->{
                    firebaseviewmodel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.my_ads)
                }
            }
            true
        }


    }

    private fun onActivityResult() {

        googleSignLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ // Регистрация launcher
          result->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val accaunt = task.getResult(ApiException::class.java)
                if (accaunt != null) {
                    dialogHelper.accaunthelper.signInFirebaseWithGoogle(accaunt.idToken!!)
                }

            } catch (e: ApiException) {
                Log.d("MyLog", "SignError:${e.message}")

            }
        }

    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_filter){
          val i = Intent(this@MainActivity,FilterActivity::class.java).apply {
              putExtra(FilterActivity.FILTER_KEY,filter)
          }// Передали MainActivity  так как внутри функции
            filterLauncher.launch(i) // Передали данные заполненные на Filter Activity если они уже были
        }

        return super.onOptionsItemSelected(item)
    }



    private fun ScrollListener() = with(binding.mainContent){ // Слушатель скролла, когда
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) { // Отслеживание на какой позиции пользователь при скролле
                super.onScrollStateChanged(recyclerView, newState)

                if(!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE){ // Проверка если скроллим вниз, и состояние покоя(как доходит до конца)
                    clearUpdate = false // При скролле будет добавлятся новые обьявления, не стирая старых
                    val adslist = firebaseviewmodel.liveadsdata.value!!
                    if(adslist.isNotEmpty()) { // Проверка, чтобы список не было 0 при скролле, иначе будет ошибка
                       CheckForCats(adslist) // Загружаем обьявления, от последнего по времени обьявления если не ищем по категории, иначе сортируется по выбранной категорией. Добрались до массива наших обьявления и от туда берем эти данные
                    }
                }

            }
        })
    }

    private fun CheckForCats(adsList : ArrayList<AdPost>){ // Функция, чтобы проверять, если мы скролим в разное, то при переходе на категорию список должен очищатся и заполнятся обьявлениями из нужной категории
        adsList[0]
            .let {
                if (currentCategory == getString(R.string.other_ads)) {
                    firebaseviewmodel.loadAllAdsNextPage(it.time,filterDb) // Берем обьявления из конца при скролле, подгружая следущие
                } else {
                    firebaseviewmodel.loadCatAdsNextPage(it.category!!,it.time,filterDb)
                }
            }

    }








    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true // При переходе на другую катерогию, будут подгружатся обьявление с этой категории с перовй странице
        when (item.itemId) {

            R.id.bt_ads -> {
                getAdsFromCat(getString(R.string.bt_ads))
                binding.mainContent.toolbar.title = getString(R.string.bt_ads)


            }
            R.id.pc_ads -> {
                getAdsFromCat(getString(R.string.pc_ads))
                binding.mainContent.toolbar.title = getString(R.string.pc_ads)

            }
            R.id.cars_ads -> {
                getAdsFromCat(getString(R.string.cars_ads))
                binding.mainContent.toolbar.title = getString(R.string.cars_ads)


            }
            R.id.smart_ads -> {
                getAdsFromCat(getString(R.string.smart_ads))
                binding.mainContent.toolbar.title = getString(R.string.smart_ads)

            }
            R.id.flat_ads -> {
                getAdsFromCat(getString(R.string.flat_ads))
                binding.mainContent.toolbar.title = getString(R.string.flat_ads)

            }
            R.id.clothes_ads -> {
                getAdsFromCat(getString(R.string.clothes_ads))
                binding.mainContent.toolbar.title = getString(R.string.clothes_ads)
            }
            R.id.handmade_ads -> {
                getAdsFromCat(getString(R.string.handmade_ads))
                binding.mainContent.toolbar.title = getString(R.string.handmade_ads)

            }
            R.id.sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)


            }
            R.id.sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)


            }
            R.id.sign_out -> {
                if(mAuth.currentUser?.isAnonymous == true){
                    binding.drawerLayout.closeDrawer(GravityCompat.START) // Добавили, чтобы закрытие drawer layout сработало
                    return true
                }  // Если пользователь который был анонимный жмет на выход, то срабатывает return
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accaunthelper.signOutWithGoogle()


            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(currentCat : String){
         currentCategory = currentCat // Запись текущей категории
         firebaseviewmodel.loadCatAdsFirstPage(filterDb,currentCat)
    }

    fun uiUpdate(user: FirebaseUser?) {
            if (user == null) {
            dialogHelper.accaunthelper.signInAnonimus(object: AccauntHelper.AccountListener{
                override fun onComplete() { // Как только успешно зарегестрировался Аноним, тогда покажет текст Гость
                 tvAccaunt.text = "Гость"
                    imAccaunt.setImageResource(R.drawable.ic_account) // Установка значения по умолчанию, чтобы при выходе и входе оно обновилось
                }

            })

        } else if(user.isAnonymous) {
            tvAccaunt.text = "Гость"
                imAccaunt.setImageResource(R.drawable.ic_account) // Установка значения по умолчанию, чтобы при выходе и входе оно обновилось


        } else if(!user.isAnonymous){
                tvAccaunt.text = user.email
                Picasso.get().load(user.photoUrl).into(imAccaunt) // Загрузка ссылки на изображение в imviiew
            }

    }

    private  fun NavViewSettings() = with(binding){
      val menu = navView.menu // Добрались до меню
     val ads_title =  menu.findItem(R.id.ads_title) // Находим нужный item по id, чтобы с ним работать
        val ads_title_span = SpannableString(ads_title.title) // С помощью класса Spannable можно менять цвет текста в меню
        ads_title_span.setSpan(ForegroundColorSpan // Установка span, берем контекст, установка нужного цвета
            (ContextCompat.getColor(this@MainActivity,R.color.elementscolor))
            ,0,ads_title.title.length,0)
        ads_title.title = ads_title_span // Уже покрашенный, готовый текст



        val ads_cat =  menu.findItem(R.id.category_title) // Находим нужный item по id, чтобы с ним работать
        val ads_cat_span = SpannableString(ads_cat.title) // С помощью класса Spannable можно менять цвет текста в меню
        ads_cat_span.setSpan(ForegroundColorSpan // Установка span, берем контекст, установка нужного цвета
            (ContextCompat.getColor(this@MainActivity,R.color.elementscolor))
            ,0,ads_cat.title.length,0)
        ads_cat.title = ads_cat_span // Уже покрашенный, готовый текст


        val ads_acc =  menu.findItem(R.id.acc_title) // Находим нужный item по id, чтобы с ним работать
        val ads_acc_span = SpannableString(ads_acc.title) // С помощью класса Spannable можно менять цвет текста в меню
        ads_acc_span.setSpan(ForegroundColorSpan // Установка span, берем контекст, установка нужного цвета
            (ContextCompat.getColor(this@MainActivity,R.color.elementscolor))
            ,0,ads_acc.title.length,0)
        ads_acc.title = ads_acc_span // Уже покрашенный, готовый текст





    }
    companion object{
        const val EditState = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
        const val FILTER_INTENT = "filter"
        const val SCROLL_UP = -1

    }

    override fun onDeleteItem(ad: AdPost) {
        firebaseviewmodel.DeleteItem(ad)
    }

    override fun AddView(ad: AdPost) {
        firebaseviewmodel.viewCount(ad)
    }

    override fun FavClick(ad: AdPost) {
        firebaseviewmodel.onFavClick(ad)
    }

}


