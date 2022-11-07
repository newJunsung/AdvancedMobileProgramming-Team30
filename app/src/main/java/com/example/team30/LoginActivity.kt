package com.example.team30

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.team30.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    lateinit var db:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        // 사용자가 입력한 이메일과 비밀번호
        var email = binding.emailEdit.text
        var password = binding.passwordEdit.text

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("로그인 실패")
            .setMessage("이메일 또는 비밀번호를 확인해주세요...")
            .setPositiveButton("확인", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    Log.d("로그인", "로그인 실패")
                }
            })
            .create()

        /*Firebase.auth.signInWithEmailAndPassword("admin@team30.com", "123456")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("########## 성공")
                } else {
                    it.exception?.message
                    println("########## 실패")
                }
            }*/

        // 회원가입 페이지로 넘어감
        binding.signUpbtn.setOnClickListener {
            var intent = Intent(baseContext, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 눌러서 로그인
        binding.loginbtn.setOnClickListener {
            // 사용자가 입력한 이메일이 db에 있는지 먼저 확인
            Log.d("email : ", email.toString())
            val docRef = db.collection("users").document(email.toString())
            Log.d("docRef : ", docRef.toString())
            docRef.get()
                .addOnSuccessListener { document ->
                    Log.d("document : ", document.toString())
                    if (document != null) {
                        Log.d("password : ", document.data?.get("password").toString())
                        // 비밀번호까지 맞으면 로그인 성공
                        if (document.data?.get("password").toString() == password.toString()) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            intent = Intent(baseContext, SNSActivity::class.java)
                            startActivity(intent) // 로그인 성공하면 피드 페이지로 넘어감
                        } else { // 비밀번호가 틀리면 다시 입력받음
                            email = binding.emailEdit.text
                            password = binding.passwordEdit.text
                            binding.emailEdit.setText("")
                            binding.passwordEdit.setText("")
                            alertDialog.show()
                        }

                    } else { // 이메일이 없는 경우
//                        Log.d(TAG, "No such document")
                        alertDialog.show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)

                }
        }
    }
}