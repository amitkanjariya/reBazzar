package com.example.rebazaar.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.adapters.AdapterAd
import com.example.rebazaar.databinding.ActivityAdSellerProfileBinding
import com.example.rebazaar.models.ModelAd
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdSellerProfileActivity : AppCompatActivity() {
    private var sellerUid = ""
    private lateinit var binding: ActivityAdSellerProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdSellerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("SELLER_PROFILE", "count 1")
        sellerUid = intent.getStringExtra("sellerUid").toString()
        loadSellerDetails()
        Log.d("SELLER_PROFILE", "count 2")

        loadAds()
        Log.d("SELLER_PROFILE", "count 3")


        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
    }

    private fun loadSellerDetails(){
        Log.d("SELLER_PROFILE", "count 4")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("SELLER_PROFILE", "count 5")
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                    binding.sellerNameTv.text = name
                    try {
                        Log.d("SELLER_PROFILE", "count 6")
                        Glide.with(this@AdSellerProfileActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.round_person_white)
                            .into(binding.sellerProfileIv)
                    } catch (e : Exception){
                        Log.d("SELLER_PROFILE", "error")
                    }
                    Log.d("SELLER_PROFILE", "count 7")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAds(){
        Log.d("SELLER_PROFILE", "count 8")
        val adArrayList: ArrayList<ModelAd> = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.orderByChild("uid").equalTo(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("SELLER_PROFILE", "count 9")
                    adArrayList.clear()
                    for (ds in snapshot.children){
                        val modelAd = ds.getValue(ModelAd::class.java)
                        adArrayList.add(modelAd!!)
                    }
                    Log.d("SELLER_PROFILE", "count 9")

                    val adapterAd = AdapterAd(this@AdSellerProfileActivity, adArrayList)
                    binding.adsRv.adapter = adapterAd
                    val adsCount = "${adArrayList.size}"
                    binding.publishedAdsCountTv.text = adsCount
                    Log.d("SELLER_PROFILE", "count 10")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}