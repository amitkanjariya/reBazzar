package com.example.rebazaar.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.rebazaar.R
import com.example.rebazaar.databinding.RowImageSliderBinding
import com.example.rebazaar.models.ModelImageSlider
import com.google.android.material.imageview.ShapeableImageView

class AdapterImageSlider : Adapter<AdapterImageSlider.HolderImageSlider> {
    private lateinit var binding : RowImageSliderBinding
    private var context: Context
    private var imageArrayList: ArrayList<ModelImageSlider>

    constructor(context: Context, imageArrayList: ArrayList<ModelImageSlider>){
        this.context = context
        this.imageArrayList = imageArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImageSlider {
        Log.d("AD_DETAIL", "adapterImageSlider 1: ")
        binding = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        Log.d("AD_DETAIL", "adapterImageSlider 2: ")
        return HolderImageSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImageSlider, position: Int) {
        Log.d("AD_DETAIL", "adapterImageSlider 3: ")
        val modelImageSlider = imageArrayList[position]
        val imageUrl = modelImageSlider.imageUrl
        val imageCount = "${position+1}/${imageArrayList.size}"
        holder.imageCountTv.text = imageCount
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_gray)
            .into(holder.imageIv)
        holder.itemView.setOnClickListener{

        }
        Log.d("AD_DETAIL", "adapterImageSlider 4: ")
    }

    inner class HolderImageSlider(itemView: View) : RecyclerView.ViewHolder(itemView){
        var imageIv : ShapeableImageView = binding.imageIv
        var imageCountTv: TextView = binding.imageCountTv
    }
}