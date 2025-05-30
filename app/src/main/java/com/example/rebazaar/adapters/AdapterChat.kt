package com.example.rebazaar.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.models.ModelChat
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class AdapterChat : RecyclerView.Adapter<AdapterChat.HolderChat> {
    private val context: Context
    private val chatArrayList: ArrayList<ModelChat>

    companion object{
        private const val MSG_TYPE_LEFT=0
        private const val MSG_TYPE_RIGHT=1
    }

    private val firebaseAuth: FirebaseAuth

    constructor(context: Context, chatArrayList: ArrayList<ModelChat>){
        this.context = context
        this.chatArrayList = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : HolderChat {
        if(viewType== MSG_TYPE_RIGHT){
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false)
            return HolderChat(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false)
            return HolderChat(view)
        }
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int){
        val modelChat = chatArrayList[position]
        val message = modelChat.message
        val messageType = modelChat.messageType
        val timestamp = modelChat.timestamp
        val formattedDate = Utils.formatTimestampDateTime(timestamp)
        holder.timeTv.text = formattedDate
        if(messageType == Utils.MESSAGE_TYPE_TEXT){
            holder.messageTv.visibility = View.VISIBLE
            holder.messageIv.visibility = View.GONE
            holder.messageTv.text = message
        } else{
            holder.messageTv.visibility = View.GONE
            holder.messageIv.visibility = View.VISIBLE
            try {
                Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.ic_image_gray)
                    .error(R.drawable.ic_image_broken_gray)
                    .into(holder.messageIv)
            } catch (e : Exception){

            }
        }
    }

    override fun getItemCount() : Int{
        return chatArrayList.size
    }

    override fun getItemViewType(position: Int) : Int {
        if(chatArrayList[position].fromUid == firebaseAuth.uid){
            return MSG_TYPE_RIGHT
        } else{
            return MSG_TYPE_LEFT
        }
    }

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messageTv : TextView = itemView.findViewById(R.id.messageTv)
        val timeTv : TextView = itemView.findViewById(R.id.timeTv)
        val messageIv : ShapeableImageView = itemView.findViewById(R.id.messageIv)
    }
}