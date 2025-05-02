package com.example.rebazaar.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.rebazaar.fragments.AccountFragment
import com.example.rebazaar.fragments.ChatsFragment
import com.example.rebazaar.fragments.HomeFragment
import com.example.rebazaar.fragments.MyAdsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            startLoginOptions()
        }

        showHomeFragment()

        binding.bottomNv.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_home -> {
                    showHomeFragment()
                    true
                }
                R.id.menu_chats -> {
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    } else{
                        showChatsFragment()
                        true
                    }
                }
                R.id.menu_my_ads -> {
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    } else{
                        showMyAdsFragment()
                        true
                    }
                }
                R.id.menu_account -> {
                    if(firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOptions()
                        false
                    } else{
                        showAccountFragment()
                        true
                    }
                }
                else -> {
                    false
                }
            }
        }
        binding.sellFab.setOnClickListener{
            val intent = Intent(this, AdCreateActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }
    }

    private fun showHomeFragment(){
        binding.toolbarTitleTV.text = "Home"
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showChatsFragment(){
        binding.toolbarTitleTV.text = "Chats"
        val fragment = ChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "ChatsFragment")
        fragmentTransaction.commit()
    }

    private fun showMyAdsFragment(){
        binding.toolbarTitleTV.text = "MyAds"
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()
    }

    private fun showAccountFragment(){
        binding.toolbarTitleTV.text = "Account"
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "AccountFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptions(){
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }
}