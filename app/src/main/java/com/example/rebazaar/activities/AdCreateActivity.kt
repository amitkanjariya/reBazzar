package com.example.rebazaar.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.rebazaar.CloudinaryManager
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.adapters.AdapterImagePicked
import com.example.rebazaar.databinding.ActivityAdCreateBinding
import com.example.rebazaar.models.ModelImagePicked
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdCreateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAdCreateBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null
    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>
    private lateinit var adapterImagePicked : AdapterImagePicked
    private var isEditMode = false
    private var adIdForEditing = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        CloudinaryManager.init(this)

        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        val adapterConditions = ArrayAdapter(this, R.layout.row_condition_act, Utils.conditions)
        binding.conditionAct.setAdapter(adapterConditions)

        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if(isEditMode){
            adIdForEditing = intent.getStringExtra("adId") ?: ""
            loadAdDetails()
            binding.toolbarTitleTV.text = "UpdateAd"
            binding.postAdBtn.text = "Update Ad"
        } else{
            binding.toolbarTitleTV.text = "Create Ad"
            binding.postAdBtn.text = "Post Ad"
        }

        imagePickedArrayList = ArrayList()
        loadImages()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.toolbarAdImageBtn.setOnClickListener{
            showImagePickOptions()
        }

        binding.postAdBtn.setOnClickListener{
            validateData()
        }

        binding.locationAct.setOnClickListener{
            val intent = Intent(this, LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }
    }

    private val locationPickerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            if(data!=null) {
                latitude = data.getDoubleExtra("latitude", 0.0)
                longitude = data.getDoubleExtra("longitude", 0.0)
                address = data.getStringExtra("address") ?: ""

                binding.locationAct.setText(address)
            }
        } else{
            Utils.toast(this, "Cancelled")
        }
    }

    private fun loadImages(){
        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList, adIdForEditing)
        binding.imagesRv.adapter = adapterImagePicked
    }

    private fun showImagePickOptions(){
        val popupMenu = PopupMenu(this, binding.toolbarAdImageBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if(itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                } else{
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if(itemId == 2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    pickImageGallery()
                } else{
                    val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }
            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        if(isGranted){
            pickImageGallery()
        } else{
            Utils.toast(this, "Storage permission denied...")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            pickImageCamera()
        } else{
            Utils.toast(this, "Camera or Storage or both permissions denied...")
        }
    }

    private fun pickImageGallery(){
        //Intent to launch Image Picker e.g. Gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        //check if image is picked or not
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            val timestamp = "${Utils.getTimeStamp()}"
            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else{
            Utils.toast(this, "Cancelled...!")
        }
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        //check if image is picked or not
        if(result.resultCode == Activity.RESULT_OK){
            val timestamp = "${Utils.getTimeStamp()}"
            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else{
            Utils.toast(this, "Cancelled...!")
        }
    }

    private var brand = ""
    private var category = ""
    private var condition = ""
    private var address = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var latitude = 0.0
    private var longitude = 0.0

    private fun validateData(){
        brand = binding.brandEt.text.toString().trim()
        category = binding.categoryAct.text.toString().trim()
        condition = binding.conditionAct.text.toString().trim()
        address = binding.locationAct.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if(brand.isEmpty()){
            binding.brandEt.error = "Enter Brand"
            binding.brandEt.requestFocus()
        } else if(category.isEmpty()){
            binding.categoryAct.error = "Choose Category"
            binding.categoryAct.requestFocus()
        } else if(condition.isEmpty()){
            binding.conditionAct.error = "Choose Condition"
            binding.conditionAct.requestFocus()
        } else if(title.isEmpty()){
            binding.titleEt.error = "Enter Title"
            binding.titleEt.requestFocus()
        } else if(description.isEmpty()){
            binding.descriptionEt.error = "Enter Description"
            binding.descriptionEt.requestFocus()
        } else {
            if(isEditMode){
                updateAd()
            } else {
                postAd()
            }
        }
    }

    private fun updateAd(){
        progressDialog.setMessage("Updating Ad...")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                uploadImagesStorage(adIdForEditing)
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update the Ad due to ${e.message}")
            }
    }

    private fun postAd(){
        progressDialog.setMessage("Publishing Ad")
        progressDialog.show()
        val timestamp = Utils.getTimeStamp()
        val refAds = FirebaseDatabase.getInstance().getReference("Ads")
        val keyId = refAds.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["status"] = "${Utils.AD_STATUS_AVAILABLE}"
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                uploadImagesStorage(keyId)
            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Utils.toast(this, "Failed due to ${e.message}")
            }
    }

    private fun uploadImagesStorage(adId: String){
        // Loop through each picked image and upload via Cloudinary
        imagePickedArrayList.forEachIndexed { index, model ->
            val filePath = Utils.getPath(this, model.imageUri!!)
            if (filePath == null) {
                Utils.toast(this, "Skipping invalid image")
                return@forEachIndexed
            }

            CloudinaryManager.uploadImage(
                context = this,
                filePath = filePath,
                folderName = "Ads/${adId}/",
                onSuccess = { url ->
                    // Save image URL under the Ad node
                    val imgMap = hashMapOf<String, Any>(
                        "id" to model.id,
                        "imageUrl" to url
                    )
                    FirebaseDatabase.getInstance()
                        .getReference("Ads")
                        .child(adId)
                        .child("Images")
                        .child(model.id)
                        .updateChildren(imgMap)

                    // If last image, dismiss dialog and finish
                    if (index == imagePickedArrayList.lastIndex) {
                        progressDialog.dismiss()
                        Utils.toast(this, "Ad published successfully")
                        finish()
                    }
                },
                onError = { err ->
                    if (index == imagePickedArrayList.lastIndex) {
                        progressDialog.dismiss()
                    }
                    Utils.toast(this, "Image upload failed: $err")
                }
            )

            // Update progress dialog
            val msg = "Uploading ${index + 1} of ${imagePickedArrayList.size} images..."
            progressDialog.setMessage(msg)
            progressDialog.show()
        }
    }

    private fun loadAdDetails(){
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val brand = "${snapshot.child("child").value}"
                    val category = "${snapshot.child("category").value}"
                    val condition = "${snapshot.child("condition").value}"
                    latitude = (snapshot.child("latitude").value as Double) ?: 0.0
                    longitude = (snapshot.child("longitude").value as Double) ?: 0.0
                    val address = "${snapshot.child("address").value}"
                    val price = "${snapshot.child("price").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"

                    binding.brandEt.setText(brand)
                    binding.categoryAct.setText(category)
                    binding.conditionAct.setText(condition)
                    binding.locationAct.setText(address)
                    binding.priceEt.setText(price)
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    val refImages = snapshot.child("Images").ref
                    refImages.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(ds in snapshot.children){
                                val id = "${ds.child("id").value}"
                                val imageUrl = "${ds.child("imageUrl").value}"
                                val modelImagePicked = ModelImagePicked(id, null, imageUrl, true)
                                imagePickedArrayList.add(modelImagePicked)
                            }
                            loadImages()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}