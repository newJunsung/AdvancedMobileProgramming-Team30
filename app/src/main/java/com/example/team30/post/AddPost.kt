package com.example.team30.post

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.team30.databinding.ActivityAddPostBinding
import com.example.team30.home.SNSActivity
import com.example.team30.post.model.PostDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*

class AddPost : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding

    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    lateinit var photoUri: Uri

    // photo picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            photoUri = uri
            binding.addpostImage.setImageURI(photoUri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore
        storage = Firebase.storage

        binding.addpostUploadImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addpostUploadButton.setOnClickListener {
            contentUpload()
        }
    }

    private fun contentUpload() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val imageRef = storage.reference.child("images/${imageFileName}")
        imageRef.putFile(photoUri).addOnSuccessListener {
            Log.d("Upload Image", "Image Upload Success")
            intent = Intent(this, SNSActivity::class.java);
            startActivity(intent)
//            imageRef.downloadUrl.addOnSuccessListener {
//                var postDTO = PostDTO()
//
//                // Insert downloadURL of image
//                postDTO.imageUrl = it.toString()
//            }
        }
    }
}