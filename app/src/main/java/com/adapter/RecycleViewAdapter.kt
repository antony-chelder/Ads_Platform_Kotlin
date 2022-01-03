package com.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.act.DescActivity
import com.act.EditAdsActivity
import com.model.AdPost
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.tony_fire.descorderkotlin.MainActivity
import com.tony_fire.descorderkotlin.R
import com.tony_fire.descorderkotlin.databinding.AdItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecycleViewAdapter(val act: MainActivity) :
    RecyclerView.Adapter<RecycleViewAdapter.AdViewHolder>() {
    val adArrayList = ArrayList<AdPost>()
    var timeFormatter : SimpleDateFormat? = null 

    init{
        timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault()) 
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = AdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding, act,timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.SetData(adArrayList[position])

    }

    override fun getItemCount(): Int {
        return adArrayList.size

    }

    fun updateAdapter(newListItem: List<AdPost>) {
//        val tempAdsList = ArrayList<AdPost>()
//        tempAdsList.addAll(adArrayList) 
//        tempAdsList.addAll(newListItem)
        val diffresult = DiffUtil.calculateDiff(DiffUtilHelper(adArrayList,newListItem))
        diffresult.dispatchUpdatesTo(this)
        adArrayList.clear()
        adArrayList.addAll(newListItem)
    }


    fun updateWithClearAdapter(newListItem: List<AdPost>) { 
        val diffresult = DiffUtil.calculateDiff(DiffUtilHelper(adArrayList,newListItem))
        diffresult.dispatchUpdatesTo(this)
        adArrayList.clear()
        adArrayList.addAll(newListItem)
    }

    class AdViewHolder(val binding: AdItemBinding, val act: MainActivity, val formatter : SimpleDateFormat) :
        RecyclerView.ViewHolder(binding.root) {

        fun SetData(ad: AdPost) = with(binding) {

            tvTitle.text = ad.title
            tvPrice.text = ad.price
            tvDesc.text = ad.description
            tvLikes.text = ad.favcounter
            val publishTime = "Время публикации : ${timeConverter(ad.time)}"
            tvPublishedTime.text = publishTime
            Picasso.get().load(ad.mainImage).into(tvImage)

            tvCounter.text = ad.viewsCounter
            checkIsFavOrNot(ad)
            shoEditPanel(isOwner(ad))
            allclickListeners(ad)

        }

        private fun timeConverter(timeMillis : String) : String { 
          val calendar = Calendar.getInstance() 
            calendar.timeInMillis = timeMillis.toLong()

            return formatter.format(calendar.time) 

        }

        private fun allclickListeners(ad:AdPost) = with(binding){
            itemView.setOnClickListener {
                act.AddView(ad) 
                val i = Intent(binding.root.context, DescActivity::class.java).apply {
                    putExtra(AD_KEY,ad) 
                }
                binding.root.context.startActivity(i)
            }

            likeButton.setOnClickListener {
                if(act.mAuth.currentUser?.isAnonymous == false) act.FavClick(ad) 

            }


            icEdit.setOnClickListener(onClickEdit(ad))
            icDelete.setOnClickListener{
                act.onDeleteItem(ad)

            }

        }

        private fun checkIsFavOrNot(ad: AdPost) = with(binding){
            if(ad.isFav){ 
                likeButton.setImageResource(R.drawable.pressed_fav_image)
            }else{
                likeButton.setImageResource(R.drawable.no_pressed_fav_image)
            }
        }
         private fun onClickEdit(ad:AdPost) : View.OnClickListener{
             return View.OnClickListener {
                 val editintent = Intent(act, EditAdsActivity::class.java).apply {
                     putExtra(MainActivity.EditState,true)
                     putExtra(MainActivity.ADS_DATA,ad)

                 }
                 act.startActivity(editintent)

             }

         }

        private fun isOwner(ad: AdPost): Boolean {
            return ad.uid == act.mAuth.uid
        }

        private fun shoEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.editPanel.visibility = View.VISIBLE
            } else {
                binding.editPanel.visibility = View.GONE
            }
        }


    }

    companion object{
        const val AD_KEY = "adkey"
    }

    interface AdItemListener{
        fun onDeleteItem(ad: AdPost)
        fun AddView(ad: AdPost)
        fun FavClick(ad: AdPost)
    }

}
