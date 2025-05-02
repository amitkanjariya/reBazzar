package com.example.rebazaar.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginEmailBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private companion object{
        private const val TAG = "LOGIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //init/setop ProgressDialog to show while sign-in
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.noAccountTV.setOnClickListener{
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        binding.forgotPasswordTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.loginBtn.setOnClickListener{
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData(){
        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "valodateData: Password: $password")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailET.error = "Invalid Email format"
            binding.emailET.requestFocus()
        } else if(password.isEmpty()){
            binding.passwordET.error = "Enter Password"
            binding.passwordET.requestFocus()
        } else{
            loginUser()
        }
    }

    private fun loginUser(){
        Log.d(TAG, "loginUser: ")
        //show progress
        progressDialog.setMessage("Logging In")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            Log.e(TAG, "loginUser: Logged In...")
            progressDialog.dismiss()

            //start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() //finish current and all activites from back stack
        }.addOnFailureListener{e ->
            Log.e(TAG, "loginUser: ", e)
            progressDialog.dismiss()
            Utils.toast(this, "Unable to login due to ${e.message}")
        }
    }
}