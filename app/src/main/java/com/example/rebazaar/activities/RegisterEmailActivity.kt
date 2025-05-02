package com.example.rebazaar.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterEmailBinding
    private companion object{
        private const val TAG = "REGISTER_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
        binding.haveAccountTv.setOnClickListener{
            onBackPressed()
        }
        binding.registerBtn.setOnClickListener{
            validateData()
        }
    }

    private var email = ""
    private var password = ""
    private var cPassword = ""

    private fun validateData(){
        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()
        cPassword = binding.cPasswordET.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")
        Log.d(TAG, "validateData: confirm password: $cPassword")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailET.error = "Invalid Email Pattern"
            binding.emailET.requestFocus()
        } else if(password.isEmpty()){
            binding.passwordET.error = "Enter password"
            binding.passwordET.requestFocus()
        } else if(cPassword.isEmpty()){
            binding.cPasswordET.error = "Enter Confirm Password"
            binding.cPasswordET.requestFocus()
        } else if(password!=cPassword){
            binding.cPasswordET.error = "Password Doesn't Match"
            binding.cPasswordET.requestFocus()
        } else{
            registerUser()
        }
    }

    private fun registerUser(){
        Log.d(TAG, "registerUser: ")
        progressDialog.setMessage("Creating Account")
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()
            }
            .addOnFailureListener{ e ->
                Log.e(TAG, "registerUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to create account dur to ${e.message}")
            }
    }
    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        progressDialog.setMessage("Saving User Info")

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email"
        hashMap["typingTo"] = ""
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: User registered...")
                progressDialog.dismiss()
                //start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener{ e->
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }
    }
}