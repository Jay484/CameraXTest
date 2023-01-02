package com.example.cameraxtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.cameraxtest.databinding.ActivityMainBinding
import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraUtil: CameraUtil
    var imageCapture : ImageCapture? = null
    var videoCapture : VideoCapture<Recorder>? = null
    var isRecording = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        cameraUtil = CameraUtil(this, contentResolver)
        requestPermission()
        startCamera()
        registerClickListeners()
    }

    private fun registerClickListeners() {
        viewBinding.btnRecord.setOnClickListener {
            recordClicked()
        }

        viewBinding.btnCapture.setOnClickListener {
            cameraUtil.capture(
                imageCapture
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.pvMain.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture)

            } catch(exc: Exception) {
                Log.e("testJay", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO),
            5
        )
    }

    private fun recordClicked(){
        if(isRecording){
            cameraUtil.stopRecordingAndSave()
            isRecording = false
            viewBinding.btnRecord.text = getString(R.string.record)

        }else {
            cameraUtil.startRecording(videoCapture)
            isRecording = true
            viewBinding.btnRecord.text = getString(R.string.stop)
        }
    }

}