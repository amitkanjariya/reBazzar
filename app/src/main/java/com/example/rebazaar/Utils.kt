package com.example.rebazaar

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

/**
 * General utility methods and constants.
 */
object Utils {
    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    val categories = arrayOf(
        "All", "Mobiles", "Computer/Laptop", "Electronics & Home Appliances", "Vehicles", "Furniture & Home Decor",
        "Fashion & Beauty", "Books", "Sports", "Animals", "Businesses", "Agriculture"
    )

    val categoryIcons = arrayOf(
        R.drawable.ic_category_all,
        R.drawable.ic_category_mobile,
        R.drawable.ic_category_computer,
        R.drawable.ic_category_electronics,
        R.drawable.ic_category_vehicles,
        R.drawable.ic_category_furniture,
        R.drawable.ic_category_fashion,
        R.drawable.ic_category_books,
        R.drawable.ic_category_sports,
        R.drawable.ic_category_animals,
        R.drawable.ic_category_business,
        R.drawable.ic_category_agriculture
    )

    val conditions = arrayOf("New", "Used", "Refurbished")

    /**
     * Show a short Toast message.
     */
    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Current epoch time in milliseconds.
     */
    fun getTimeStamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Format a timestamp (ms) into dd/MM/yyyy string.
     */
    fun formatTimestampDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    /**
     * Convert a Uri into a local file path by copying its content into a temp file.
     * Returns null on failure.
     */
    fun getPath(context: Context, uri: Uri): String? {
        val fileName = getFileName(context, uri) ?: return null
        val tempFile = File(context.cacheDir, fileName)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract the display name of the file represented by Uri.
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    fun addToFavorite(context: Context, adId: String){
        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null){
            Utils.toast(context, "You're not logged-in!")
        } else{
            val timestamp = Utils.getTimeStamp()
            val hashMap = HashMap<String, Any>()
            hashMap["adId"] = adId
            hashMap["timestamp"] = timestamp
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .setValue(hashMap)
                .addOnSuccessListener {
                    Utils.toast(context, "Added to favorite...!")
                }
                .addOnFailureListener{ e ->
                    Utils.toast(context, "Failed to add to favorite due to ${e.message}")
                }
        }
    }

    fun removeFromFavorite(context: Context, adId: String){
        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null){
            Utils.toast(context, "You're not logged-in!")
        } else{
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .removeValue()
                .addOnSuccessListener {
                    Utils.toast(context, "Remove from favorite...!")
                }
                .addOnFailureListener{ e ->
                    Utils.toast(context, "Failed to remove from favorite due to ${e.message}")
                }
        }
    }

    fun callIntent(context: Context, phone: String){
        Log.d("AD_DETAIL", "util callIntent: ")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    fun smsIntent(context: Context, phone: String){
        Log.d("AD_DETAIL", "util smsIntent: ")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    fun mapIntent(context: Context, latitude: Double, longitude: Double){
        Log.d("AD_DETAIL", "util mapIntent: ")
        val gmmIntentUri = Uri.parse("http://maps.google.com/?daddr=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if(mapIntent.resolveActivity(context.packageManager)!=null){
            context.startActivity(mapIntent)
        } else{
            Utils.toast(context, "Google Map not installed!")
        }
    }
}
