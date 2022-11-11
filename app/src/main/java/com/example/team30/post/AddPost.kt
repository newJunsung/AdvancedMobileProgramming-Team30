package com.example.team30.post

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.team30.databinding.ActivityAddPostBinding
import com.example.team30.home.SNSActivity
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
    lateinit var auth: FirebaseAuth

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
        auth = Firebase.auth

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

        // Callback
        imageRef.putFile(photoUri).addOnSuccessListener {
            Log.d("Upload Image", "Image Upload Success")
            intent = Intent(this, SNSActivity::class.java);
            startActivity(intent)
            imageRef.downloadUrl.addOnSuccessListener {
                var postDTO = PostDTO()

                // Insert downloadURL of image
                postDTO.imageUrl = it.toString()
                // Insert uid of user
                postDTO.uid = auth.currentUser?.uid
                // Insert userId
                postDTO.userId = auth.currentUser?.email
                // Insert title
                postDTO.title = binding.addpostTitle.text.toString()
                // Insert description
                postDTO.description = binding.addpostDescription.text.toString()
                // Insert timestamp
                postDTO.timestamp = System.currentTimeMillis()

                db.collection("posts").document().set(postDTO)

                Toast.makeText(this, "글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                Log.d("Upload Post", "Post Upload Success")
                setResult(Activity.RESULT_OK)
                finish()
            }
        }.addOnFailureListener {
            Log.d("Upload Post", "Post Upload Fail")
        }
    }
}