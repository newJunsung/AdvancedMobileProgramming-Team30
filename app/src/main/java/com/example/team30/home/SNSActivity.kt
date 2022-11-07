package com.example.team30.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.team30.R
import com.example.team30.databinding.ActivitySnsBinding
import com.example.team30.home.feeds.FeedsFragment
import com.example.team30.home.friends.FriendsFragment
import com.example.team30.home.profile.ProfileFragment
import com.google.android.material.navigation.NavigationBarView

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
        feedsFragment = FeedsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, feedsFragment).commit()
    }

    // 탭바를 선택하면 해당 fragment로 이동
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tab_bar_feeds -> {
                feedsFragment = FeedsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, feedsFragment).commit()
            }
            R.id.tab_bar_profile -> {
                profileFragment = ProfileFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, profileFragment).commit()
            }
            R.id.tab_bar_friends -> {
                friendsFragment = FriendsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, friendsFragment).commit()
            }
        }

        return true
    }
}