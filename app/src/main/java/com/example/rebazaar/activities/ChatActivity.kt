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
import com.example.rebazaar.CloudinaryManager
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.adapters.AdapterChat
import com.example.rebazaar.databinding.ActivityChatBinding
import com.example.rebazaar.models.ModelChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var receiptUid = ""
    private var myUid = ""
    private var chatPath = ""
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        CloudinaryManager.init(this)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        receiptUid = intent.getStringExtra("receiptUid")!!
        myUid = firebaseAuth.uid!!
        chatPath = Utils.chatPath(receiptUid, myUid)
        loadReceiptDetails()
        loadMessages()
        binding.toolbarBackBtn.setOnClickListener{
            finish()
        }
        binding.attachFab.setOnClickListener{
            imagePickDialag()
        }
        binding.sendFab.setOnClickListener{
            validateData()
        }
    }

    private fun loadReceiptDetails(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                    binding.toolbarTitleTv.text = name
                    Glide.with(this@ChatActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.round_person_white)
                        .into(binding.toolbarProfileIv)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadMessages(){
        val messageArrayList = ArrayList<ModelChat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatPath)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageArrayList.clear()
                    for(ds: DataSnapshot in snapshot.children){
                        val modelChat = ds.getValue(ModelChat::class.java)
                        messageArrayList.add(modelChat!!)
                    }
                    val adapterChat = AdapterChat(this@ChatActivity, messageArrayList)
                    binding.chatRv.adapter = adapterChat
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialag(){
        val popupMenu = PopupMenu(this, binding.attachFab)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId
            if(itemId==1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else{
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            } else if(itemId==2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    pickImageGallery()
                } else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    private fun pickImageCamera(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        //check if image is picked or not
        if(result.resultCode == Activity.RESULT_OK){
            uploadToCloudinary()
        } else{
            Utils.toast(this, "Cancelled...!")
        }
    }

    private fun pickImageGallery(){
        //Intent to launch Image Picker e.g. Gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        //check if image is picked or not
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            uploadToCloudinary()
        } else{
            Utils.toast(this, "Cancelled...!")
        }
    }

    private fun uploadToCloudinary() {
        Log.d("IMAGE_UPLOAD", "test 1")
        if (imageUri == null) {
            Log.d("IMAGE_UPLOAD", "test 2")
            Utils.toast(this, "Image URI is null")
            return
        }
        Log.d("IMAGE_UPLOAD", "test 3")
        val filePath = Utils.getPath(this, imageUri!!)
        Log.d("IMAGE_UPLOAD", "test 4")
        if (filePath == null) {
            Log.d("IMAGE_UPLOAD", "test 5")
            Utils.toast(this, "Failed to get file path")
            return
        }
        Log.d("IMAGE_UPLOAD", "test 6")

        val timestamp = Utils.getTimeStamp()
        Log.d("IMAGE_UPLOAD", "test 7")
        progressDialog.setMessage("Uploading to Cloudinary...")
        progressDialog.show()
        Log.d("IMAGE_UPLOAD", "test 8")

        CloudinaryManager.uploadImage(
            context = this,
            filePath = filePath,
            folderName = "chat_images/",
            onSuccess = { uploadedImageUrl ->
                Log.d("IMAGE_UPLOAD", "test 9")
                progressDialog.dismiss()
                sendMessage(Utils.MESSAGE_TYPE_IMAGE, uploadedImageUrl, timestamp)
                Log.d("IMAGE_UPLOAD", "test 10")

            },
            onError = { errorMessage ->
                Log.d("IMAGE_UPLOAD", "test 11")
                progressDialog.dismiss()
                Utils.toast(this, "Upload failed: $errorMessage")
            }
        )
        Log.d("IMAGE_UPLOAD", "test 12")

    }


    private fun validateData(){
        val message = binding.messageEt.text.toString().trim()
        val timestamp = Utils.getTimeStamp()
        if(message.isEmpty()){
            Utils.toast(this, "Enter message to send ...")
        } else {
            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp)
        }
    }

    private fun sendMessage(messageType: String, message: String, timestamp: Long){
        progressDialog.setMessage("Sending message...!")
        progressDialog.show()
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()
        hashMap["messageId"] = "$keyId"
        hashMap["messageType"] = "$messageType"
        hashMap["message"] = "$message"
        hashMap["fromUid"] = "$myUid"
        hashMap["toUid"] = "$receiptUid"
        hashMap["timestamp"] = timestamp

        refChat.child(chatPath)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.messageEt.setText("")
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Utils.toast(this, "Failed to send due to ${e.message}")
            }
    }
}