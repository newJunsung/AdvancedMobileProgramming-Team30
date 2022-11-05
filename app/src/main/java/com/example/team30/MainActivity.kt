package com.example.team30

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.team30.databinding.ActivityMainBinding
import com.example.team30.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.auth.signInWithEmailAndPassword("admin@team30.com", "123456")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("########## 성공")
                } else {
                    it.exception?.message
                    println("########## 실패")
                }
            }
    }
}