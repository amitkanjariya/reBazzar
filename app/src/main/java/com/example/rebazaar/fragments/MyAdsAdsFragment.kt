package com.example.rebazaar.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rebazaar.adapters.AdapterAd
import com.example.rebazaar.databinding.FragmentMyAdsAdsBinding
import com.example.rebazaar.models.ModelAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyAdsAdsFragment : Fragment() {
    private lateinit var binding: FragmentMyAdsAdsBinding

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adArrayList: ArrayList<ModelAd>
    private lateinit var adapterAd: AdapterAd


    override fun onAttach(context: Context){
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        loadAds()
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                adapterAd.getFilter().filter(query)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun loadAds(){
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        adArrayList = ArrayList()
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot){
                    adArrayList.clear()
                    for(ds in snapshot.children){
                        try {
                            val modelAd = ds.getValue(ModelAd::class.java)
                            adArrayList.add(modelAd!!)
                        } catch(e : Exception){
                            Log.e("AdsAds", "onDataChange: ", e)
                        }
                    }
                    adapterAd = AdapterAd(mContext, adArrayList)
                    binding.adsRv.adapter = adapterAd
                }

                override fun onCancelled(error: DatabaseError){

                }
            })
    }
}