package com.example.rebazaar.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.rebazaar.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.rebazaar.CloudinaryManager
import com.example.rebazaar.R
import com.example.rebazaar.Utils

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private companion object{
        private const val TAG = "PROFILE_EDIT_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var myUserType = ""
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        // Init Cloudinary
        CloudinaryManager.init(this)

        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
        binding.profileImagePickFab.setOnClickListener{
            imagePickDialog()
        }
        binding.updateBtn.setOnClickListener{
            validateData()
        }
    }

    private var name = ""
    private var dob = ""
    private var email = ""

    private fun validateData(){
        name = binding.nameEt.text.toString().trim()
        dob = binding.dobEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        if(imageUri == null){
            updateProfileDb(null)
        } else{
            uploadProfileImageStorage()
        }
    }

    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: Using Cloudinary now...")
        progressDialog.setMessage("Uploading user profile image")
        progressDialog.show()

        val filePath = Utils.getPath(this, imageUri!!) // Changed: resolve path via Utils
        if (filePath == null) {
            progressDialog.dismiss()
            Utils.toast(this, "Failed to get image path")
            return
        }

        CloudinaryManager.uploadImage(
            context = this,
            filePath = filePath,
            onSuccess = { uploadedImageUrl ->
                progressDialog.dismiss()
                updateProfileDb(uploadedImageUrl)
            },
            onError = { errorMessage ->
                progressDialog.dismiss()
                Utils.toast(this, "Failed to upload: $errorMessage")
            }
        )
    }

    private fun updateProfileDb(uploadedIamgeUrl : String?){
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedIamgeUrl")
        progressDialog.setMessage("Updating user info")
        progressDialog.show()

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = "$name"
        hashMap["dob"] = "$dob"
        if(uploadedIamgeUrl!=null){
            hashMap["profileImageUrl"] = "$uploadedIamgeUrl"
        }
        if(myUserType.equals("Phone", true)){
            hashMap["email"] = "$email"
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: Updated...")
                progressDialog.dismiss()
                Utils.toast(this, "Updated...")
                imageUri = null
            }
            .addOnFailureListener{ e ->
                Log.e(TAG, "updateProfileDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to update due to ${e.message}")
            }
    }

    private fun loadMyInfo(){
        Log.d(TAG, "loadMyInfo")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val progileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    myUserType = "${snapshot.child("uerType").value}"

                    if(myUserType.equals("Email", true) || myUserType.equals("Google", true)){
                        binding.emailTil.isEnabled = false
                        binding.emailTil.isEnabled = false
                    }
                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(progileImageUrl)
                            .placeholder(R.drawable.round_person_white)
                            .into(binding.profileIV)
                    } catch (e : Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this, binding.profileImagePickFab)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if(itemId == 1){
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else{
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if(itemId == 2){
                Log.d(TAG, "imagePickDialog: Galery Clicked, check if storage permission granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    pickImageGallery()
                } else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){  result ->
        Log.d(TAG, "reuestCameraPermissions: result: $result")

        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            Log.d(TAG, "requestCameraPermissions: All granted e.g.Camera, Storage")
            pickImageCamera()
        } else{
            Log.d(TAG, "requestCameraPermissions: All or either one is denied...")
            Utils.toast(this, "Camera or storage or both permission denied")
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted $isGranted")
        if(isGranted){
            pickImageGallery()
        }  else{
            Utils.toast(this, "Storage permission denied...")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri")
            try {
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.round_person_white)
                    .into(binding.profileIV)
            } catch (e : Exception){
                Log.e(TAG, "cameraActivityResultLauncher: ", e)
            }
        } else{
            Utils.toast(this, "Cancelled!")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            try{
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.round_person_white)
                    .into(binding.profileIV)
            } catch (e : Exception){
                Log.e(TAG, "galleryActivityResultLauncher: ", e)
            }
        }
    }

}