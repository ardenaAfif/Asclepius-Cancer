package com.dicoding.cancer.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.cancer.R
import com.dicoding.cancer.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    private val uri get() = Uri.parse(intent.getStringExtra(EXTRA_URI))
    private val name get() = intent.getStringExtra(EXTRA_NAME)
    private val score get() = String.format("%.0f",intent.getFloatExtra(EXTRA_SCORE,0F) * 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        resultView()
    }

    private fun resultView() = with(binding) {
        // Menampilkan hasil gambar, prediksi, dan confidence score
        resultImage.setImageURI(uri)
        resultText.text = "$name $score%"
    }

    companion object {
        const val EXTRA_URI = "EXTRA_URI"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_SCORE = "EXTRA_SCORE"
    }
}