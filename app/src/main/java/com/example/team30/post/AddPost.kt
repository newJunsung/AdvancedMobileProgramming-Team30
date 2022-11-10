package com.example.team30.post

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.team30.databinding.ActivityAddPostBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddPost : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding

    lateinit var db: FirebaseFirestore
    lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        binding.addpostUploadButton.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {

    }
}