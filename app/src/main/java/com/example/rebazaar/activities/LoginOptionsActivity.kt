package com.example.rebazaar.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOptionsBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private companion object{
        private const val TAG = "LOGIN_OPTIONS_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init/setup ProgressDialog to show while sign-up
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wail...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.closeBtn.setOnClickListener{
            onBackPressed()
        }
        binding.loginEmailBtn.setOnClickListener{
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }
        binding.loginGoogleBtn.setOnClickListener{
            beginGoogleLogin()
        }
        binding.loginPhoneBtn.setOnClickListener{
            startActivity(Intent(this, LoginPhoneActivity::class.java))
        }
    }

    private fun beginGoogleLogin(){
        Log.d(TAG, "beginGoogleLogin: ")
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        Log.d(TAG, "googleSignInARL: ")

        if(result.resultCode == RESULT_OK){
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                //get the GoogleSignInAccount from intent
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account Id: ${account.id}")
                firebaseAuthGoogleAccount(account.idToken)
            } catch (e: Exception){
                Log.e(TAG, "googleSignInARL: ", e)
                Utils.toast(this, "${e.message}")
            }
        } else{
            Utils.toast(this, "Cancelled...!")
        }
    }

    private fun firebaseAuthGoogleAccount(idToken : String?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: $idToken")
        //setup google credential to sign in with firebase auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //firebase auth with google credential is succed, let's check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User, Account created...")
                    //New user, Account created. Let's save user info to firebase realtime database
                    updateUserInfoDb()
                } else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User, Logged In...")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener{ e ->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e)
                Utils.toast(this, "${e.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")
        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser?.email
        val registeredUserUid = firebaseAuth.currentUser?.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Google"
        hashMap["typingTo"] = ""
        hashMap["onlineStatus"] = timestamp
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserUid

        //set data to firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User info saved")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Log.e(TAG, "updateUserInfoDb: ", e)
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }
    }
}