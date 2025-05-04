package com.example.rebazaar

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryManager {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = hashMapOf(
                "cloud_name" to "dmqe5s7ld",
                "api_key" to "431179467514736",
                "api_secret" to "EacETQQfIXi6bfs_TSPpCP_cPgk"
            )
            MediaManager.init(context.applicationContext, config)
            isInitialized = true
        }
    }

    fun uploadImage(
        context: Context,
        filePath: String,
        folderName: String = "profile_pics/",
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            MediaManager.get().upload(filePath)
                .option("folder", folderName)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("IMAGE_UPLOAD", "Upload started")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.d("IMAGE_UPLOAD", "Uploading $bytes / $totalBytes")
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        Log.d("IMAGE_UPLOAD", "Upload success: $resultData")
                        val url = resultData?.get("secure_url") as? String
                        if (url != null) onSuccess(url) else onError("URL missing in response")
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.d("IMAGE_UPLOAD", "Upload failed: ${error?.description}")
                        onError(error?.description ?: "Unknown error")
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Exception during upload: ${e.message}")
        }


    }
}
