package com.example.team30.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.team30.databinding.ActivitySignupBinding
import com.example.team30.home.SNSActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    lateinit var db: FirebaseFirestore

    companion object {
        const val BASIC_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/team30-project.appspot.com/o/userProfileImages%2FBasic.png?alt=media&token=c8dc87bc-405d-442a-ba5a-993077099854"
        // 이메일 검사 정규식
        private val emailValidation =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = binding.nameEdit.text
        val email = binding.emailSignUpEdit.text
        val password = binding.pwSignUpEdit.text

        // 입력하는 글자색 검정으로
        binding.nameEdit.setTextColor(Color.BLACK)
        binding.emailSignUpEdit.setTextColor(Color.BLACK)
        binding.pwSignUpEdit.setTextColor(Color.BLACK)

        // emailSignUpEdit 에 TextWatcher 연결
        emailSignUpEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // text가 변경된 후 호출
                // p0 : 변경 후 문자열
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // text가 변경되기 전 호출
                // p0 : 변경 전 문자열
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // text가 바뀔 때마다 호출
                checkEmail()
            }
        })

        // cloud firestore 초기화
        db = Firebase.firestore

        binding.signUpbtn2.setOnClickListener { // 회원가입 버튼 누르면
            doSignUp(email.toString(), password.toString(), name.toString()) // 회원가입
            // 유저 이름 받아서
            val user = hashMapOf (
                "name" to name.toString()
            )

//            // firestore 에 유저 이름 추가
//            db.collection("users").document(name.toString())
//                .set(user)
//                .addOnSuccessListener { documentReference ->
//                    //Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//                }
//                .addOnFailureListener { e ->
//                    Log.w(TAG, "Error adding document", e)
//                }

            // 회원가입 중복 확인.. 추가
            /*val docRef = db.collection("users").document(email.toString())
            Log.d("docRef : ", docRef.toString())*/


            // db 추가하고 다시 로그인 페이지로 넘어감
            /*var intent = Intent(baseContext, LoginActivity::class.java)
            startActivity(intent)*/
        }
    }

    private fun doSignUp(userEmail: String, password: String, name: String) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Firebase.auth.signInWithEmailAndPassword(userEmail, password).addOnSuccessListener {
                        var map = HashMap<String, Any>()
                        map["image"] = BASIC_IMAGE_URL
                        map["email"] = userEmail
                        map["name"] =  name
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        db.collection("profileImages").document(uid)
                            .set(map)
                    }
                    startActivity(
                        Intent(this, SNSActivity::class.java)
                    )
                    finish()
                } else {
                    Log.w("LoginActivity", "signUpWithEmail", it.exception)
                    Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 이메일 형식 검사
    fun checkEmail(): Boolean {
        var email = binding.emailSignUpEdit.text.toString()
        val pattern = Pattern.matches(emailValidation, email) // 서로 패턴이 맞는지

        if (pattern) { // 이메일 형태가 맞으면
            binding.joinEmailValidation.text = ""
            return true
        } else { // 이메일 형태가 아니면 텍스트뷰를 동적으로 생성하고 아래에 문구를 띄운다.
            binding.joinEmailValidation.text = "* 이메일 형식을 확인해주세요!"
            binding.joinEmailValidation.setTextColor(-65536)
            return false
        }
    }
}