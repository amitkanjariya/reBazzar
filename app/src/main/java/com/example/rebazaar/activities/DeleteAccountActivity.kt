package com.example.rebazaar.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        loadUserProfileImage()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener{
            deleteAccount()
        }
    }

    private fun loadUserProfileImage() {
        val uid = firebaseAuth.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .child("profileImageUrl")
            .get()
            .addOnSuccessListener { snapshot ->
                val url = snapshot.getValue(String::class.java)
                if (!url.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.round_person_white)
                        .into(binding.profileIV)
                }
            }
            .addOnFailureListener { e ->
                // Optional: log or toast error
            }
    }

    public fun deleteAccount(){
        progressDialog.setMessage("Deleting User Account")
        progressDialog.show()
        val myUid = firebaseAuth.uid
        firebaseUser!!.delete()
            .addOnSuccessListener {
                progressDialog.setMessage("Deleting User Ads")
                val refUserAds = FirebaseDatabase.getInstance().getReference("Ads")
                refUserAds.orderByChild("uid").equalTo(myUid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(ds in snapshot.children){
                                ds.ref.removeValue()
                            }
                            progressDialog.setMessage("Deleting User Data")
                            val refUsers = FirebaseDatabase.getInstance().getReference("Users")
                            refUsers.child(myUid!!).removeValue()
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    startMainActivity()
                                }
                                .addOnFailureListener{ e ->
                                    progressDialog.dismiss()
                                    Utils.toast(
                                        this@DeleteAccountActivity,
                                        "Failed to delete user data due to ${e.message}"
                                    )
                                    startMainActivity()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Utils.toast(this, "Failed to delete user data due to ${e.message}")
            }
    }

    private fun startMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()    //clear back-stack of activites
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startMainActivity()
    }
}