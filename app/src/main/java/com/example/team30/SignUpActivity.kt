package com.example.team30

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.team30.databinding.ActivitySignupBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = binding.nameEdit.text
        val email = binding.emailSignUpEdit.text
        val password = binding.pwSignUpEdit.text

        // cloud firestore 초기화
        db = Firebase.firestore

        binding.signUpbtn2.setOnClickListener { // 회원가입 버튼 누르면
            // 유저 정보 모아서
            val user = hashMapOf (
                "name" to name.toString(),
                "email" to email.toString(),
                "password" to password.toString()
            )

            // firestore 에 데이터 추가
            db.collection("users").document(email.toString())
                .set(user)
                .addOnSuccessListener { documentReference ->
                    //Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

            // 회원가입 중복 확인.. 추가
            val docRef = db.collection("users").document(email.toString())
            Log.d("docRef : ", docRef.toString())


            // db 추가하고 다시 로그인 페이지로 넘어감
            var intent = Intent(baseContext, LoginActivity::class.java)
            startActivity(intent)
        }


    }
}