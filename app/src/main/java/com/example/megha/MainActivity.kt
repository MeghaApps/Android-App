package com.example.megha

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.megha.ml.MeghaModel
import org.tensorflow.lite.support.image.TensorImage
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var captureButton: ImageView
    private lateinit var predictedTextLabel: TextView
    private lateinit var predictedTextScore: TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var scanAnother: TextView
    private lateinit var button: com.google.android.material.button.MaterialButton
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)
        captureButton = findViewById(R.id.capture)
        predictedTextLabel = findViewById(R.id.predicted_text_label)
        predictedTextScore = findViewById(R.id.predicted_text_score)
        linearLayout = findViewById(R.id.linear_layout)
        scanAnother = findViewById(R.id.scan_another)
        button = findViewById(R.id.button)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        }
        captureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
        button.setOnClickListener {
            val intent = Intent(this,Clouds::class.java)
            startActivity(intent)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Log.e("Error", e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val aspectRatio = imageBitmap.width / imageBitmap.height.toFloat()
            val width = 700
            val height = (width / aspectRatio).roundToInt()
            val newBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true)
            captureButton.setImageBitmap(newBitmap)
            linearLayout.visibility = INVISIBLE
            scanAnother.visibility = VISIBLE
            findTypeOfCloud(imageBitmap)
            scanAnother.setOnClickListener {
                dispatchTakePictureIntent()
            }
        }
    }

    private fun findTypeOfCloud(imageBitmap: Bitmap) {
        val model = MeghaModel.newInstance(this)
        val image = TensorImage.fromBitmap(imageBitmap)
        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList
        var maxScore = 0.0
        var cloudCategory = ""

        for (cloud in probability) {
            val score = cloud.score
            val category = cloud.label
            if (score > maxScore) {
                maxScore = score.toDouble()
                cloudCategory = category
            }
        }
        when (cloudCategory) {
            "Ac" -> {
                val value = "Altocumulus"
                predictedTextLabel.text = value
            }
            "As" -> {
                val value = "Altostratus"
                predictedTextLabel.text = value
            }
            "Cb" -> {
                val value = "Cumulonimbus"
                predictedTextLabel.text = value
            }
            "Cc" -> {
                val value = "Cirrocumulus"
                predictedTextLabel.text = value
            }
            "Ci" -> {
                val value = "Cirrus"
                predictedTextLabel.text = value
            }
            "Cs" -> {
                val value = "Cirrostratus"
                predictedTextLabel.text = value
            }
            "Ct" -> {
                val value = "Contrial"
                predictedTextLabel.text = value
            }
            "Cu" -> {
                val value = "Cumulus"
                predictedTextLabel.text = value
            }
            "Ns" -> {
                val value = "Nimbostratus"
                predictedTextLabel.text = value
            }
            "Sc" -> {
                val value = "Stratocumulus"
                predictedTextLabel.text = value
            }
            "St" -> {
                val value = "Stratus"
                predictedTextLabel.text = value
            }
        }
        predictedTextScore.text = maxScore.toString()
        model.close()
    }
}