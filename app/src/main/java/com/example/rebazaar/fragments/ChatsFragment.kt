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
import com.example.rebazaar.R
import com.example.rebazaar.adapters.AdapterChats
import com.example.rebazaar.databinding.FragmentChatsBinding
import com.example.rebazaar.models.ModelChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {
    private lateinit var binding : FragmentChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private lateinit var mContext: Context
    private lateinit var chatsArrayList: ArrayList<ModelChats>
    private lateinit var adapterChats: AdapterChats

    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
        loadChats()
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                adapterChats.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun loadChats(){
        chatsArrayList=ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                for(ds in snapshot.children){
                    val chatKey = "${ds.key}"
                    if(chatKey.contains(myUid)){
                        val modelChats = ModelChats()
                        modelChats.chatKey=chatKey
                        chatsArrayList.add(modelChats)
                    }
                }
                adapterChats = AdapterChats(mContext, chatsArrayList)
                binding.chatsRv.adapter = adapterChats
                sort()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sort(){
        Handler().postDelayed({
            chatsArrayList.sortWith{ model1: ModelChats, model2:ModelChats ->
                model2.timestamp.compareTo(model1.timestamp)
            }
            adapterChats.notifyDataSetChanged()
        }, 1000)
    }
}