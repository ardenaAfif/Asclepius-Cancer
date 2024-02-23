package com.dicoding.cancer.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dicoding.cancer.databinding.ActivityMainBinding
import com.dicoding.cancer.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonAction()

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {

                override fun onError(error: String) {
                    runOnUiThread {
                        binding.progressIndicator.isVisible = false
                        enableButton()
                        showToast(error)
                    }
                }

                override fun onResults(results: List<Classifications>?) {
                    runOnUiThread {
                        Log.d(TAG, results.toString())

                        binding.progressIndicator.isVisible = false
                        enableButton()

                        val result = results
                            ?.first()
                            ?.categories
                            ?.first { it.score > 0.5 }

                        moveToResult(result)
                    }
                }
            }
        )
    }

    // Handle uCrop result
    private val launcherCropImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val croppedImageUri = UCrop.getOutput(result.data!!)
            currentImageUri = croppedImageUri
            showImage() // Update the preview with the cropped image
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.let {
                Log.e(TAG, "uCrop Error: ${it.localizedMessage}")
            }
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    // Add a function to start uCrop
    private fun startCrop(imageUri: Uri) {
        val options = UCrop.Options()

        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(imageUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .start(this)  // Memanggil launcherCropImage untuk menangani hasil pemangkasan
    }

    private fun buttonAction() {
        binding.apply {
            galleryButton.setOnClickListener {
                startGallery()
            }
            analyzeButton.setOnClickListener {
                analyzeImage()
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "show Image: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        binding.progressIndicator.isVisible = true
        if (currentImageUri == null) return

        imageClassifierHelper.classifyStaticImage(currentImageUri!!)
    }

    private fun moveToResult(data: Category?) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_NAME, data?.label)
        intent.putExtra(ResultActivity.EXTRA_SCORE, data?.score)
        intent.putExtra(ResultActivity.EXTRA_URI, currentImageUri.toString())
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun disableButton() = with(binding) {
        galleryButton.isEnabled = false
        analyzeButton.isEnabled = false
    }

    private fun enableButton() = with(binding) {
        galleryButton.isEnabled = true
        analyzeButton.isEnabled = currentImageUri != null
    }

    companion object {
        const val TAG = "MainActivity"
    }
}