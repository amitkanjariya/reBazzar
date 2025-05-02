package com.example.rebazaar.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.rebazaar.activities.ChangePasswordActivity
import com.example.rebazaar.activities.DeleteAccountActivity
import com.example.rebazaar.activities.MainActivity
import com.example.rebazaar.activities.ProfileEditActivity
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object{
        private const val TAG = "ACCOUNT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context){
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()
        binding.logoutCv.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }
        binding.editProfileCv.setOnClickListener{
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }
        binding.changePasswordCv.setOnClickListener{
            startActivity(Intent(mContext, ChangePasswordActivity::class.java))
        }
        binding.verifiyAccountCv.setOnClickListener{
            verifyAccount()
        }
        binding.deleteAccountCv.setOnClickListener{
            startActivity(Intent(mContext, DeleteAccountActivity::class.java))
        }
    }

    private fun loadMyInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val progileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("uerType").value}"

                    val phone = phoneCode+phoneNumber
                    if(timestamp=="null"){
                        timestamp="0"
                    }

                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                    binding.emailTv.text=email
                    binding.nameTv.text=name
                    binding.dobTv.text=dob
                    binding.phoneTv.text=phone
                    binding.memberSinceTv.text=formattedDate

                    if(userType == "Email"){
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        if(isVerified){
                            binding.verifiyAccountCv.visibility = View.GONE
                            binding.verificationTv.text="Verified"
                        } else{
                            binding.verifiyAccountCv.visibility = View.VISIBLE
                            binding.verificationTv.text="Not Verified"
                        }
                    } else{
                        binding.verifiyAccountCv.visibility = View.GONE
                        binding.verificationLabelTv.text="Verified"
                    }

                    try {
                        Glide.with(mContext)
                            .load(progileImageUrl)
                            .placeholder(R.drawable.round_person_white)
                            .into(binding.profileIV)
                    } catch (e : Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun verifyAccount(){
        progressDialog.setMessage("Sending account verification instructions to your email...")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Utils.toast(mContext, "Account verification instructions sent to your email...")
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Utils.toast(mContext, "Failed to send due to ${e.message}")
            }
    }
}