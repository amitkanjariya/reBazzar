package com.example.rebazaar

import android.content.Context
import android.net.Uri
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
        MediaManager.get().upload(filePath)
            .option("folder", folderName)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("URL not found in response.")
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Unknown error occurred")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}
