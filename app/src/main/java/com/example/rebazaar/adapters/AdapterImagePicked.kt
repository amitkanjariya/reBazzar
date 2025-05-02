package com.example.rebazaar.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.RowImagesPickedBinding
import com.example.rebazaar.models.ModelImagePicked
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.internal.Util

class AdapterImagePicked(private val context : Context, private val imagesPickedArrayList: ArrayList<ModelImagePicked>, private val adId: String)
    : Adapter<AdapterImagePicked.HolderImagePicked>() {


    private lateinit var binding: RowImagesPickedBinding

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderImagePicked {
        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagePicked(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        val model = imagesPickedArrayList[position]


        if(model.fromInternet){
            try {
                val imageUrl = model.imageUrl
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            } catch (e : Exception){
                Log.e("IMAGES_TAG", "onBindViewHolder: ", e)
            }
        } else{
            try {
                val imageUri = model.imageUri
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            } catch (e : Exception){
                Log.e("IMAGES_TAG", "onBindViewHolder: ", e)
            }
        }


        holder.closeBtn.setOnClickListener{
            if(model.fromInternet){
                deleteImageFirebase(model, holder, position)
            } else{
                imagesPickedArrayList.remove(model)
                notifyDataSetChanged()
            }
        }
    }

    private fun deleteImageFirebase(model: ModelImagePicked, holder: AdapterImagePicked.HolderImagePicked, position: Int){
        val imageId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images").child(imageId)
            .removeValue()
            .addOnSuccessListener {
                Utils.toast(context, "Image Deleted")
                try {
                    imagesPickedArrayList.remove(model)
                    notifyItemRemoved(position)
                } catch (e : Exception){
                    Log.e("deleteImage", "deleteImageFirebase2: ", e)
                }
            }
            .addOnFailureListener{ e ->
                Utils.toast(context, "Failed to delete image due to ${e.message}")
            }
    }

    override fun getItemCount(): Int {
        return imagesPickedArrayList.size
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView) {
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }

}