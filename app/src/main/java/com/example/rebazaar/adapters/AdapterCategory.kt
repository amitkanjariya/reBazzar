package com.example.rebazaar.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.rebazaar.RvListenerCategory
import com.example.rebazaar.databinding.RowCategoryBinding
import com.example.rebazaar.models.ModelCategory
import java.util.Random

class AdapterCategory (
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
) : Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: RowCategoryBinding
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]
        val icon = modelCategory.icon
        val category = modelCategory.category
        val random = Random()
        val color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

        holder.categoryIconIv.setImageResource(icon)
        holder.categoryTv.text = category
        holder.categoryIconIv.setBackgroundColor(color)
        holder.itemView.setOnClickListener{
            rvListenerCategory.onCategoryClick(modelCategory)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View): ViewHolder(itemView){
        var categoryIconIv = binding.categoryIconIv
        var categoryTv = binding.categoryTv
    }


}