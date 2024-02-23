package com.dicoding.cancer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.cancer.R
import com.dicoding.cancer.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
    }


}