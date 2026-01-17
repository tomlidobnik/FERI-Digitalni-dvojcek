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
    private var eventId: Int? = null

    private val API_BASE_URL = MyApplication.dotenv["API_BASE_URL"]

    // Cache OkHttpClient to avoid recreating it on every request
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getInt(ARG_EVENT_ID, -1).takeIf { id -> id != -1 }
        }
        Log.d(TAG, "CameraFragment created with event ID: $eventId")
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

        // Disable take photo button until camera is ready
        buttonTakePhoto.isEnabled = false

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
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

        // Start camera initialization asynchronously after view is laid out
        view.post {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Optimize preview for better performance with explicit resolution
                val preview = Preview.Builder()
                    .setTargetRotation(cameraPreview.display.rotation)
                    .setTargetResolution(android.util.Size(1280, 720)) // Limit preview resolution
                    .build()
                    .also {
                        it.setSurfaceProvider(cameraPreview.surfaceProvider)
                    }

                // Optimize image capture settings with specific resolution
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(cameraPreview.display.rotation)
                    .setTargetResolution(android.util.Size(1920, 1080)) // Balanced capture resolution
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

                // Enable take photo button once camera is ready
                buttonTakePhoto.isEnabled = true
                Log.d(TAG, "Camera initialized")

            } catch (exc: Exception) {
                Log.e(TAG, "Camera binding failed", exc)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show()
                }
                buttonTakePhoto.isEnabled = false
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Disable button immediately to prevent multiple clicks
        buttonTakePhoto.isEnabled = false

        val photoFile = File(
            requireContext().cacheDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor, // Use background executor to avoid blocking main thread
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
                        buttonTakePhoto.isEnabled = true
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageFile = photoFile
                    Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                    displayCapturedImage(photoFile)
                }
            }
        )
    }

    private fun displayCapturedImage(imageFile: File) {
        buttonTakePhoto.isEnabled = false

        CoroutineScope(Dispatchers.Default).launch {
            var bitmap: Bitmap? = null
            var rotatedBitmap: Bitmap? = null

            try {
                // Get dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(imageFile.absolutePath, options)

                // Calculate sample size and decode with optimal settings
                options.inSampleSize = calculateInSampleSize(options, 720, 1280)
                options.inJustDecodeBounds = false
                options.inPreferredConfig = Bitmap.Config.RGB_565

                bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
                    ?: throw Exception("Failed to decode bitmap")

                // Rotate and recycle original in one pass
                rotatedBitmap = rotateBitmap(bitmap)
                if (bitmap !== rotatedBitmap) bitmap.recycle()

                withContext(Dispatchers.Main) {
                    if (isAdded) { // Check if fragment is still attached
                        capturedImageView.setImageBitmap(rotatedBitmap)
                        cameraPreview.visibility = View.GONE
                        capturedImageView.visibility = View.VISIBLE
                        buttonTakePhoto.visibility = View.GONE
                        buttonRetake.visibility = View.VISIBLE
                        buttonSendApi.visibility = View.VISIBLE
                    } else {
                        rotatedBitmap?.recycle()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Image error: ${e.message}")
                bitmap?.recycle()
                rotatedBitmap?.recycle()

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error displaying image", Toast.LENGTH_SHORT).show()
                        buttonTakePhoto.isEnabled = true
                    }
                }
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    private fun retakePhoto() {
        // Clean up captured image
        capturedImageFile?.delete()
        capturedImageFile = null

        // Clear the ImageView and recycle bitmap to free memory
        capturedImageView.setImageBitmap(null)

        cameraPreview.visibility = View.VISIBLE
        capturedImageView.visibility = View.GONE
        buttonTakePhoto.visibility = View.VISIBLE
        buttonTakePhoto.isEnabled = true
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

                // Build multipart form data matching backend Form parameters
                val multipartBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        imageFile.name,
                        imageBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("confidence", "0.5")

                // Add event_id as Form parameter if available
                eventId?.let {
                    multipartBuilder.addFormDataPart("event_id", it.toString())
                }

                val request = Request.Builder()
                    .url("$API_BASE_URL/detect")
                    .post(multipartBuilder.build())
                    .build()

                Log.d(TAG, "Making API call...")
                val response = httpClient.newCall(request).execute() // Reuse cached client!
                Log.d(TAG, "Response: ${response.code}")

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonSendApi.isEnabled = true

                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "API Response: $responseBody")

                        val numPeople = parseNumPeopleFromResponse(responseBody)
                        val message = if (numPeople >= 0) {
                            "Detection complete!\nPeople detected: $numPeople"
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
                        Toast.makeText(
                            requireContext(),
                            "API Error: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonSendApi.isEnabled = true
                    val errorMessage = when {
                        e.message?.contains("Failed to connect") == true -> "Cannot connect to server"
                        e.message?.contains("timeout") == true -> "Connection timeout. Try again."
                        else -> "Network error: ${e.message}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the ImageView to free bitmap memory
        capturedImageView.setImageBitmap(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        capturedImageFile?.delete()
    }

    override fun onPause() {
        super.onPause()
        // Unbind camera to free resources when fragment is paused
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        // Only restart camera if it was paused and preview is still visible
        if (cameraPreview.visibility == View.VISIBLE && cameraProvider == null && allPermissionsGranted()) {
            view?.post {
                startCamera()
            }
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val ARG_EVENT_ID = "event_id"

        @JvmStatic
        fun newInstance(eventId: Int? = null) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    eventId?.let { putInt(ARG_EVENT_ID, it) }
                }
            }
    }
}

