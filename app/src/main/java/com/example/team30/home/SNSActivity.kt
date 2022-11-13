package com.example.team30.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.team30.R
import com.example.team30.databinding.ActivitySnsBinding
import com.example.team30.home.feeds.FeedsFragment
import com.example.team30.home.friends.FriendsFragment
import com.example.team30.home.profile.ProfileFragment
import com.example.team30.post.AddPost
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sns.*
import kotlinx.android.synthetic.main.activity_sns.view.*

class SNSActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    private lateinit var binding: ActivitySnsBinding

    private lateinit var feedsFragment: FeedsFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var friendsFragment: FriendsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySnsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomTabBar.setOnItemSelectedListener(this)
        setDefaultToolbar()
        feedsFragment = FeedsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, feedsFragment).commit()
    }

    // 탭바를 선택하면 해당 fragment로 이동
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setDefaultToolbar()
        when (item.itemId) {
            R.id.tab_bar_feeds -> {
                feedsFragment = FeedsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, feedsFragment).commit()
            }
            R.id.tab_bar_profile -> {
                profileFragment = ProfileFragment.newInstance()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid", uid)
                profileFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, profileFragment).commit()
            }
            R.id.tab_bar_friends -> {
                friendsFragment = FriendsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, friendsFragment).commit()
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
            R.id.toolbar_post -> {
                intent = Intent(baseContext, AddPost::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}