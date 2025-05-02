package com.example.rebazaar.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.adapters.AdapterImageSlider
import com.example.rebazaar.databinding.ActivityAdDetailsBinding
import com.example.rebazaar.models.ModelAd
import com.example.rebazaar.models.ModelImageSlider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var adId = ""
    private var adLatitude = 0.0
    private var adLongitude = 0.0
    private var sellerUid = ""
    private var sellerPhone = ""
    private var favorite = false
    private lateinit var imageSliderArrayList: ArrayList<ModelImageSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("AD_DETAIL", "0001")
        binding.toolbarEditBtn.visibility = View.GONE
        binding.toolbarDeleteBtn.visibility = View.GONE
        binding.callBtn.visibility = View.GONE
        binding.chatBtn.visibility = View.GONE
        binding.smsBtn.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()
        adId = intent.getStringExtra("adId").toString()
        Log.d("AD_DETAIL", "onCreate: adId: $adId")
        if(firebaseAuth.currentUser!=null){
            checkIsFavorite()
        }
        Log.d("AD_DETAIL", "0002")
        loadAdDetails()
        Log.d("AD_DETAIL", "0003")
        loadAdImages()
        Log.d("AD_DETAIL", "0004")
        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
        binding.toolbarDeleteBtn.setOnClickListener{
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            materialAlertDialogBuilder.setTitle("DeleteAd")
                .setMessage("Are you sure? You want to delete this Ad?")
                .setPositiveButton("DELETE"){ dialog, which ->
                    Log.d("AD_DETAIL", "onCreate: delete clicked...")
                    deleteAd()
                }
                .setNegativeButton("CANCEL"){ dialog, which ->
                    Log.d("AD_DETAIL", "onCreate: cancel clicked...")

                    dialog.dismiss()
                }
                .show()
        }

        binding.toolbarEditBtn.setOnClickListener{
            editOptionsDialog()
        }

        binding.toolbarFavBtn.setOnClickListener{
            if(favorite){
                Utils.removeFromFavorite(this, adId)
            } else{
                Utils.addToFavorite(this, adId)
            }
        }

        binding.sellerProfileCv.setOnClickListener{
            val intent = Intent(this, AdSellerProfileActivity::class.java)
            intent.putExtra("sellerUid", sellerUid)
            startActivity(intent)
        }

        binding.chatBtn.setOnClickListener{

        }

        binding.callBtn.setOnClickListener{
            Utils.callIntent(this, sellerPhone)
        }

        binding.smsBtn.setOnClickListener{
            Utils.smsIntent(this, sellerPhone)
        }

        binding.mapBtn.setOnClickListener{
            Utils.mapIntent(this, adLatitude, adLongitude)
        }
    }

    private fun editOptionsDialog(){
        val popupMenu = PopupMenu(this, binding.toolbarEditBtn)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Edit")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Mark as Sold")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId
            if(itemId==0){
                val intent = Intent(this, AdCreateActivity::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("adId", adId)
                startActivity(intent)
            } else if(itemId==1){
                showMarkAsSoldDialog()
            }
            return@setOnMenuItemClickListener true
        }

    }

    private fun showMarkAsSoldDialog(){
        Log.d("AD_DETAIL", "showMarkAsSoldDialog: ")
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        alertDialogBuilder.setTitle("Mark as sold")
            .setMessage("Are you sure? You want to mark this Ad as sold?")
            .setPositiveButton("SOLD"){ dialog, which ->
                val hashMap = HashMap<String, Any>()
                hashMap["status"] = "${Utils.AD_STATUS_SOLD}"
                val ref = FirebaseDatabase.getInstance().getReference("Ads")
                ref.child(adId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Utils.toast(this, "Marked as sold")
                    }
                    .addOnFailureListener{
                        Utils.toast(this, "Failed to mark as sold")
                    }
            }
            .setNegativeButton("CANCEL"){ dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAdDetails(){
        Log.d("AD_DETAIL", "LoadAdDetails: ")
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modelAd = snapshot.getValue(ModelAd::class.java)
                    sellerUid = "${modelAd!!.uid}"
                    val title = modelAd.title
                    val description = modelAd.description
                    val address = modelAd.address
                    val condition = modelAd.condition
                    val price = modelAd.price
                    val category = modelAd.category
                    adLatitude = modelAd.latitude
                    adLongitude = modelAd.longitude
                    val timestamp = modelAd.timestamp
                    val formattedDate = Utils.formatTimestampDate(timestamp)
                    if(sellerUid==firebaseAuth.uid){
                        binding.toolbarEditBtn.visibility=View.VISIBLE
                        binding.toolbarDeleteBtn.visibility=View.VISIBLE
                        binding.chatBtn.visibility=View.GONE
                        binding.callBtn.visibility=View.GONE
                        binding.smsBtn.visibility=View.GONE
                        binding.sellerProfileLabelTv.visibility=View.GONE
                        binding.sellerProfileCv.visibility=View.GONE
                    } else{
                        binding.toolbarEditBtn.visibility=View.GONE
                        binding.toolbarDeleteBtn.visibility=View.GONE
                        binding.chatBtn.visibility=View.VISIBLE
                        binding.callBtn.visibility=View.VISIBLE
                        binding.smsBtn.visibility=View.VISIBLE
                        binding.sellerProfileLabelTv.visibility=View.VISIBLE
                        binding.sellerProfileCv.visibility=View.VISIBLE
                    }
                    binding.titleTv.text=title
                    binding.descriptionTv.text=description
                    binding.addressTv.text=address
                    binding.conditionTv.text=condition
                    binding.categoryTv.text=category
                    binding.priceTv.text=price
                    binding.dateTv.text=formattedDate
                    loadSellerDetails()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadSellerDetails(){
        Log.d("AD_DETAIL", "loadSellerDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        Log.d("AD_DETAIL", "loadSellerDetails 1: ")

        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
//                    var timestamp = "${snapshot.child("timestamp").value}"
//                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                    sellerPhone="$phoneCode$phoneNumber"
                    binding.sellerNameTv.text=name

//                    binding.memberSinceTv.text=formattedDate

                    Glide.with(this@AdDetailsActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.round_person_white)
                        .into(binding.sellerProfileIv)

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        Log.d("AD_DETAIL", "loadSellerDetails 4: ")

    }

    private fun checkIsFavorite(){
        Log.d("AD_DETAIL", "checkIsFavourite: ")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favorites").child(adId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorite = snapshot.exists()
                    if(favorite){
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else{
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdImages(){
        Log.d("AD_DETAIL", "loadAdImages")
        imageSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()
                    for(ds in snapshot.children){
                        val modelImageSlider = ds.getValue(ModelImageSlider::class.java)
                        imageSliderArrayList.add(modelImageSlider!!)
                    }
                    val adapterImageSlider = AdapterImageSlider(this@AdDetailsActivity, imageSliderArrayList)
                    binding.imageSliderVp.adapter = adapterImageSlider
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun deleteAd(){
        Log.d("AD_DETAIL", "deleteAd: ")
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .removeValue()
            .addOnSuccessListener {
                Utils.toast(this, "Deleted...!")
                finish()
            }
            .addOnFailureListener{ e->
                Utils.toast(this, "Failed to delete due to ${e.message}")
            }
    }
}