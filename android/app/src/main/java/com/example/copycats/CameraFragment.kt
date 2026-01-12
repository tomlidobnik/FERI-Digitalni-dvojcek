package com.example.copycats

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var buttonTakePhoto: MaterialButton
    private lateinit var buttonRetake: MaterialButton
    private lateinit var buttonSendApi: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var capturedImageFile: File? = null

    private val API_BASE_URL = "http://10.0.2.2:8000"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraPreview = view.findViewById(R.id.camera_preview)
        capturedImageView = view.findViewById(R.id.captured_image)
        buttonTakePhoto = view.findViewById(R.id.button_take_photo)
        buttonRetake = view.findViewById(R.id.button_retake)
        buttonSendApi = view.findViewById(R.id.button_send_api)
        progressBar = view.findViewById(R.id.progress_bar)

        cameraExecutor = Executors.newSingleThreadExecutor()

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MainPageFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonTakePhoto.setOnClickListener {
            takePhoto()
        }

        buttonRetake.setOnClickListener {
            retakePhoto()
        }

        buttonSendApi.setOnClickListener {
            sendImageToApi()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(requireContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().cacheDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageFile = photoFile
                    displayCapturedImage(photoFile)
                }
            }
        )
    }

    private fun displayCapturedImage(imageFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            val rotatedBitmap = rotateBitmap(bitmap)

            capturedImageView.setImageBitmap(rotatedBitmap)

            cameraPreview.visibility = View.GONE
            capturedImageView.visibility = View.VISIBLE
            buttonTakePhoto.visibility = View.GONE
            buttonRetake.visibility = View.VISIBLE
            buttonSendApi.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.e(TAG, "Error displaying image", e)
            Toast.makeText(requireContext(), "Error displaying image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun retakePhoto() {
        capturedImageFile?.delete()
        capturedImageFile = null

        cameraPreview.visibility = View.VISIBLE
        capturedImageView.visibility = View.GONE
        buttonTakePhoto.visibility = View.VISIBLE
        buttonRetake.visibility = View.GONE
        buttonSendApi.visibility = View.GONE

        if (cameraProvider == null) {
            startCamera()
        }
    }

    private fun parseNumPeopleFromResponse(responseBody: String?): Int {
        if (responseBody == null) return -1

        try {
            // Simple JSON parsing to extract num_people
            val numPeopleRegex = """"num_people"\s*:\s*(\d+)""".toRegex()
            val matchResult = numPeopleRegex.find(responseBody)
            return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: -1
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            return -1
        }
    }

    private fun sendImageToApi() {
        val imageFile = capturedImageFile ?: run {
            Toast.makeText(requireContext(), "No image to send", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        buttonSendApi.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting API request to $API_BASE_URL/detect")

                val imageBytes = imageFile.readBytes()
                Log.d(TAG, "Image bytes read: ${imageBytes.size} bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        imageFile.name,
                        imageBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("confidence", "0.5") // Default confidence threshold
                    .build()

                val request = Request.Builder()
                    .url("$API_BASE_URL/detect")
                    .post(requestBody)
                    .build()

                /*val request = Request.Builder()
                    .url("$API_BASE_URL/")
                    .get()
                    .build()*/

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        activity?.runOnUiThread {
                            progressBar.visibility = View.GONE
                            buttonSendApi.isEnabled = true
                            Toast.makeText(requireContext(), "Request Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "OkHttp Failure", e)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()

                        activity?.runOnUiThread {
                            progressBar.visibility = View.GONE
                            buttonSendApi.isEnabled = true

                            if (response.isSuccessful) {
                                Log.d(TAG, "API Response: $responseBody")
                                val numPeople = parseNumPeopleFromResponse(responseBody)
                                val message = if (numPeople >= 0) {
                                    "Detection complete! People detected: $numPeople"
                                } else {
                                    "Detection complete!"
                                }
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                                view?.postDelayed({
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerView, MainPageFragment())
                                        .commit()
                                }, 2000)

                            } else {
                                Log.e(TAG, "API Error: ${response.code} - $responseBody")
                                Toast.makeText(requireContext(), "API Error: ${response.code}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.javaClass.simpleName} - ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonSendApi.isEnabled = true
                    val errorMessage = when {
                        e.message?.contains("Failed to connect") == true -> "Cannot connect to server. Is the API running?"
                        e.message?.contains("timeout") == true -> "Connection timeout. Please try again."
                        e.message == null -> "Network error occurred. Please check your connection and try again."
                        else -> "Network error: ${e.message}"
                    }
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        capturedImageFile?.delete()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}

