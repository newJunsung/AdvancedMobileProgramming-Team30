package com.example.team30.login

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.team30.home.SNSActivity
import com.example.team30.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    lateinit var db: FirebaseFirestore
    lateinit var alertDialog: AlertDialog

    // 이메일 검사 정규식
    private val emailValidation =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        // 사용자가 입력한 이메일과 비밀번호
        var email = binding.emailEdit.text
        var password = binding.passwordEdit.text

        // 입력하는 글자색 검정으로
        binding.emailEdit.setTextColor(Color.BLACK)
        binding.passwordEdit.setTextColor(Color.BLACK)

        // emailEdit 에 TextWatcher 연결
        emailEdit.addTextChangedListener(object : TextWatcher {
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

        alertDialog = AlertDialog.Builder(this)
            .setTitle("로그인 실패")
            .setMessage("이메일 또는 비밀번호를 확인해주세요.")
            .setPositiveButton("확인", object : DialogInterface.OnClickListener {
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
            doLogin(email.toString(), password.toString())
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun doLogin(userEmail: String, password: String) {
        // 이메일 형식 다시 한 번 체크
        if (!checkEmail()) { // 이메일 형식이 아닐 경우
            Toast.makeText(this, "이메일 형식을 확인해주세요!", Toast.LENGTH_SHORT).show()
        }
        Firebase.auth.signInWithEmailAndPassword(userEmail.toString(), password.toString())
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, SNSActivity::class.java)
                    )
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    //Toast.makeText(this, "입력한 정보를 다시 확인해주세요", Toast.LENGTH_SHORT).show()
                    alertDialog.show()
                }
            }
    }

    fun moveSNSActivity(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, SNSActivity::class.java))
        }
        finish()
    }

    // 이메일 형식 검사
    fun checkEmail(): Boolean {
        var email = binding.emailEdit.text.toString()
        val pattern = Pattern.matches(emailValidation, email) // 서로 패턴이 맞는지

        if (pattern) { // 이메일 형태가 맞으면
            binding.emailValidation.text = ""
            return true
        } else { // 이메일 형태가 아니면 텍스트뷰를 동적으로 생성하고 아래에 문구를 띄운다.
            binding.emailValidation.text = "* 이메일 형식을 확인해주세요!"
            binding.emailValidation.setTextColor(-65536)
            return false
        }
    }
}