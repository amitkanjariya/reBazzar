package com.example.rebazaar.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rebazaar.FilterChats
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.activities.ChatActivity
import com.example.rebazaar.databinding.RowChatsBinding
import com.example.rebazaar.models.ModelChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterChats : RecyclerView.Adapter<AdapterChats.HolderChats>, Filterable{
    private var context : Context
    var chatsArrayList: ArrayList<ModelChats>
    private var filterList : ArrayList<ModelChats>
    private var filter: FilterChats? = null
    private var firebaseAuth: FirebaseAuth
    private lateinit var binding: RowChatsBinding
    private var myUid = ""

    constructor(context: Context, chatsArrayList: ArrayList<ModelChats>){
        this.context=context
        this.chatsArrayList=chatsArrayList
        this.filterList=chatsArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterChats.HolderChats {
        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {
        return chatsArrayList.size
    }

    override fun onBindViewHolder(holder: AdapterChats.HolderChats, position: Int) {
        val modelChats = chatsArrayList[position]
        loadLastMessage(modelChats, holder)
        holder.itemView.setOnClickListener{
            val receiptUid = modelChats.receiptUid
            if(receiptUid!=null){
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("receiptUid", receiptUid)
                context.startActivity(intent)
            }
        }
    }

    private fun loadLastMessage(modelChats: ModelChats, holder: AdapterChats.HolderChats){
        val chatKey = modelChats.chatKey
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val fromUid = "${ds.child("fromUid").value}"
                        val message = "${ds.child("message").value}"
                        val messageId = "${ds.child("messageId").value}"
                        val messageType = "${ds.child("messageType").value}"
                        val timestamp = ds.child("timestamp").value as Long ?: 0
                        val toUid = "${ds.child("toUid").value}"
                        val formattedDate = Utils.formatTimestampDateTime(timestamp)
                        modelChats.message = message
                        modelChats.messageId = messageId
                        modelChats.messageType = messageType
                        modelChats.fromUid = fromUid
                        modelChats.timestamp = timestamp
                        modelChats.toUid = toUid

                        holder.dateTimeTv.text = "$formattedDate"
                        if(messageType==Utils.MESSAGE_TYPE_TEXT){
                            holder.lastMessageTv.text = message
                        } else{
                            holder.lastMessageTv.text = "Sends Attachment"
                        }
                    }
                    loadReceiptUserInfo(modelChats, holder)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadReceiptUserInfo(modelChats: ModelChats, holder: HolderChats){
        val fromUid = modelChats.fromUid
        val toUid = modelChats.toUid
        var receiptUid = ""
        if(fromUid==myUid){
            receiptUid=toUid
        } else{
            receiptUid=fromUid
        }
        modelChats.receiptUid = receiptUid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    modelChats.name = name
                    modelChats.profileImageUrl = profileImageUrl
                    holder.nameTv.text = name
                    Glide.with(context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(holder.profileIv)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getFilter(): Filter {
        if(filter==null){
            filter = FilterChats(this, filterList)
        }
        return filter as FilterChats
    }

    inner class HolderChats(itemView : View) : RecyclerView.ViewHolder(itemView){
        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var lastMessageTv = binding.lastMessageTv
        var dateTimeTv = binding.dateTimeTv
    }

}