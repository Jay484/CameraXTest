package com.example.cameraxtest

import android.Manifest.permission.RECORD_AUDIO
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.text.SimpleDateFormat
import java.util.*


class CameraUtil(val context: Context, val contentResolver: ContentResolver){
    var recording: Recording? = null
    companion object{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    fun capture(imageCapture: ImageCapture?) {
        if (imageCapture == null)
            return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        context,
                        "Cant capture image ${exception.message}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            }

        )
    }

    fun startRecording(videoCapture: VideoCapture<Recorder>?){
        if(videoCapture == null)
            return

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .apply {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
//                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Log.e("testJay", "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                    }
                }
            }
    }

    fun stopRecordingAndSave() {
        recording?.stop()
        recording = null
    }
}