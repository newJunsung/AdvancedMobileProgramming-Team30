package com.example.team30.home

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.team30.R
import com.example.team30.databinding.ActivitySnsBinding
import com.example.team30.home.alarms.AlarmFragment
import com.example.team30.home.feeds.FeedsFragment
import com.example.team30.home.followers.SeeFollowersFragment
import com.example.team30.home.friends.FriendsFragment
import com.example.team30.home.profile.ProfileFragment
import com.example.team30.post.AddPost
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sns.*
import kotlinx.android.synthetic.main.activity_sns.view.*

class SNSActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    private lateinit var binding: ActivitySnsBinding

    private lateinit var feedsFragment: FeedsFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var friendsFragment: FriendsFragment
    private lateinit var alarmfragment: AlarmFragment
    private lateinit var seefollowersFragment: SeeFollowersFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySnsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomTabBar.setOnItemSelectedListener(this)
        setDefaultToolbar()
        feedsFragment = FeedsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, feedsFragment).commit()

        // 툴바는 기본적으로 숨기기
        binding.toolbar.visibility = View.GONE

        val channel = NotificationChannel(
            "firebase-messaging", "firebase-messaging channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Test"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // 탭바를 선택하면 해당 fragment로 이동
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setDefaultToolbar()
        when (item.itemId) {
            R.id.tab_bar_feeds -> {
                binding.toolbar.visibility = View.GONE // 피드로 돌아갈 때 툴바 숨기기
                feedsFragment = FeedsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, feedsFragment).commit()
            }
            R.id.tab_bar_profile -> {
                binding.toolbar.visibility = View.GONE // 자기 프로필 화면으로 돌아갈 때 툴바 숨기기
                profileFragment = ProfileFragment.newInstance()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid", uid)
                profileFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, profileFragment).commit()
            }
            R.id.tab_bar_friends -> {
                binding.toolbar.visibility = View.GONE // 팔로우 목록으로 돌아갈 때 툴바 숨기기
                friendsFragment = FriendsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, friendsFragment).commit()
            }
            R.id.tab_bar_alarm -> {
                binding.toolbar.visibility = View.GONE // 알림 목록으로 돌아갈 때 툴바 숨기기
                alarmfragment = AlarmFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, alarmfragment).commit()
            }
            R.id.tab_bar_followers -> {
                binding.toolbar.visibility = View.GONE // 팔로워 목록으로 돌아갈 때 툴바 숨기기
                seefollowersFragment = SeeFollowersFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, seefollowersFragment).commit()

            }
        }

        return true
    }

    fun setDefaultToolbar() {
        toolbar_back_button.visibility = View.GONE
        toolbar_user_id.visibility = View.GONE
        toolbar_logo.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feed_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_post -> { // 사진, 글 업로드하는 페이지로 넘어간다.
                intent = Intent(baseContext, AddPost::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ProfileFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener {
                var name = "null"
                var email = "null"

                var map = HashMap<String, Any>()
                map["image"] = it.toString()

                // 해쉬맵에 Image uri만 설정했을 경우, 기존 name과 email 필드가 사라져서,
                // 파이어베이스에서 name email 필드를 불러와서 그 값을 map에 함께 넣어서 set하도록 함.
                FirebaseFirestore.getInstance().collection("profileImages").document(uid!!).get().addOnCompleteListener {
                    name = it.result!!["name"].toString()
                    email = it.result!!["email"].toString()
                    Log.d("name, email >>> ", "$name, $email")
                    map["name"] = name
                    map["email"] = email
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
                }
            }
        }
    }
}