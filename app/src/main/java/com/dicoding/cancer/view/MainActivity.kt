package com.dicoding.cancer.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dicoding.cancer.databinding.ActivityMainBinding
import com.dicoding.cancer.helper.ImageClassifierHelper
import com.dicoding.cancer.helper.getImageUri
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        imageClassifier()
    }

    private fun imageClassifier() {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {

                override fun onError(error: String) {
                    runOnUiThread {
                        binding.progressIndicator.isVisible = false
                        showToast(error)
                    }
                }

                override fun onResults(results: List<Classifications>?) {
                    runOnUiThread {
                        Log.d(TAG, results.toString())

                        binding.progressIndicator.isVisible = false

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
    private fun startCrop() {
        currentImageUri?.let {
            val tempFile = File.createTempFile("crop_image", ".png")
            val destinationUri = Uri.fromFile(tempFile)
            val intent = UCrop.of(it, destinationUri)
                .withAspectRatio(1F, 1F)
                .getIntent(this)
            launcherCropImage.launch(intent)
        }
    }

    private fun buttonAction() {
        binding.apply {
            galleryButton.setOnClickListener {
                startGallery()
            }
            linearCrop.setOnClickListener {
                if (currentImageUri == null) {
                    showToast("Please select an image first")
                } else {
                    startCrop()
                }
            }
            linearSample.setOnClickListener {
                showSampleDialog()
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
        if (currentImageUri == null) {
            showToast("Please select an image first")
            binding.progressIndicator.isVisible = false
            return
        }

        imageClassifierHelper.classifyStaticImage(currentImageUri!!)
    }

    private fun moveToResult(data: Category?) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_NAME, data?.label)
        intent.putExtra(ResultActivity.EXTRA_SCORE, data?.score)
        intent.putExtra(ResultActivity.EXTRA_URI, currentImageUri.toString())
        startActivity(intent)
    }

    // Sample Image Dialog
    private fun showSampleDialog() {
        SampleImageDialog { resId ->
            if (resId != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.progressIndicator.isVisible = true
                    invalidateOptionsMenu()

                    val uri = withContext(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeResource(resources, resId)
                        getImageUri(bitmap)
                    }

                    binding.progressIndicator.isVisible = false
                    currentImageUri = uri
                    showImage()
                }
            }
        }.show(supportFragmentManager, null)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}