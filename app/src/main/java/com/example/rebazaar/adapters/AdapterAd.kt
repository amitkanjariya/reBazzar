package com.example.rebazaar.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rebazaar.FilterAd
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.activities.AdDetailsActivity
import com.example.rebazaar.databinding.RowAdBinding
import com.example.rebazaar.models.ModelAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable{
    private lateinit var binding: RowAdBinding
    private var context : Context

    private companion object{
        private const val TAG = "ADAPTER_AD_TAG"
    }
    var adArrayList: ArrayList<ModelAd>
    private var filterList: ArrayList<ModelAd>

    private var filter: FilterAd? = null

    private var firebaseAuth : FirebaseAuth

    constructor(context: Context, adArrayList: ArrayList<ModelAd>){
        this.context = context
        this.adArrayList = adArrayList
        this.filterList = adArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAd(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        val modelAd = adArrayList[position]
        val title = modelAd.title
        val description = modelAd.description
        val address = modelAd.address
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadAdFirstImage(modelAd, holder)

        if(firebaseAuth.currentUser!=null){
            checkIsFavorite(modelAd, holder)
        }
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate
        holder.conditionTv.text = condition
        holder.addressTv.text = address

        holder.itemView.setOnClickListener{
            Log.d("AD_DETAIL", "adapterAd: start")
            val intent = Intent(context, AdDetailsActivity::class.java)
            intent.putExtra("adId", modelAd.id)
            Log.d("AD_DETAIL", "adapterAd: mid")
            context.startActivity(intent)
            Log.d("AD_DETAIL", "adapterAd: end")
        }

        holder.favBtn.setOnClickListener{
            val favorite = modelAd.favorite
            if(favorite){
                Utils.removeFromFavorite(context, modelAd.id)
            } else{
                Utils.addToFavorite(context, modelAd.id)
            }
        }
    }

    private fun checkIsFavorite(modelAd: ModelAd, holder: HolderAd){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(modelAd.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorite = snapshot.exists()
                    modelAd.favorite = favorite
                    if(favorite){
                        holder.favBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else{
                        holder.favBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd){
        val adId = modelAd.id
        Log.d(TAG, "loadAdFirstImage: adId: $adId")
        val reference = FirebaseDatabase.getInstance().getReference("Ads")
        reference.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val imageUrl = "${ds.child("imageUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        } catch (e: Exception){

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "sizeOfArraylist : ${adArrayList.size}")
        return adArrayList.size
    }



    inner class HolderAd(itemView : View) : RecyclerView.ViewHolder(itemView){
        var imageIv = binding.imageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        val favBtn = binding.favBtn
        var addressTv = binding.addressTv
        var conditionTv = binding.conditionTv
        var priceTv = binding.priceTv
        var dateTv = binding.dateTv
    }

    override fun getFilter(): Filter {
        if(filter==null){
            filter = FilterAd(this, filterList)
        }
        Log.d("FILTER_CHECK", "adapterAd: getFilter $filter")
        return filter as FilterAd
    }
}