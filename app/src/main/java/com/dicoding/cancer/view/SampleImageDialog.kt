package com.dicoding.cancer.view

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.dicoding.cancer.R
import com.dicoding.cancer.helper.toPx
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SampleImageDialog(
private val resultUri: ((Int?) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var containerView: LinearLayoutCompat? = null

    private val sampleImages by lazy {
        listOf(
            R.drawable.sample_cancer_1,
            R.drawable.sample_cancer_2,
            R.drawable.sample_non_cancer_1,
            R.drawable.sample_non_cancer_2,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = createRootView()
        createView()
        return root
    }

    private fun createRootView(): View {
        val root = NestedScrollView(requireContext())
        containerView = LinearLayoutCompat(requireContext()).apply {
            orientation = LinearLayoutCompat.VERTICAL
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            )
            setPadding(16.toPx())
            gravity = Gravity.CENTER
        }
        root.addView(containerView!!)
        return root
    }

    private fun createView() {
        if (containerView == null) return

        // spacer vertical
        val spacerV = View(requireContext())
        spacerV.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            16.toPx()
        )

        // title
        val title = TextView(requireContext())
        title.text = "Sample Image"
        title.textSize = 24F
        title.typeface = Typeface.DEFAULT_BOLD

        // add title
        containerView?.addView(title)
        // add space
        containerView?.addView(spacerV)
        // add image
        sampleImages.forEach { resId ->
            val image = ImageView(requireContext())
            image.layoutParams = LinearLayoutCompat.LayoutParams(
                200.toPx(),
                200.toPx(),
            )
            image.setImageResource(resId)
            image.scaleType = ImageView.ScaleType.CENTER_CROP
            image.setPadding(8.toPx())
            image.setOnClickListener {
                resultUri?.invoke(resId)
                dismiss()
            }

            containerView?.addView(image)
        }
    }
}
