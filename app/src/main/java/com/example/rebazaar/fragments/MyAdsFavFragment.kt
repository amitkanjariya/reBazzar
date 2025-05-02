package com.example.rebazaar.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rebazaar.adapters.AdapterAd
import com.example.rebazaar.databinding.FragmentMyAdsFavBinding
import com.example.rebazaar.models.ModelAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyAdsFavFragment : Fragment() {
    private lateinit var binding: FragmentMyAdsFavBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adArrayList: ArrayList<ModelAd>
    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false)
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
        adArrayList = ArrayList()
        val favRef = FirebaseDatabase.getInstance().getReference("Users")
        favRef.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adArrayList.clear()
                    for(ds in snapshot.children){
                        val adId = "${ds.child("adId").value}"
                        val adRaf = FirebaseDatabase.getInstance().getReference("Ads")
                        adRaf.child(adId)
                            .addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val modelAd = snapshot.getValue(ModelAd::class.java)
                                    adArrayList.add(modelAd!!)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                    Handler().postDelayed({
                        adapterAd = AdapterAd(mContext, adArrayList)
                        binding.adsRv.adapter = adapterAd
                    }, 500)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}