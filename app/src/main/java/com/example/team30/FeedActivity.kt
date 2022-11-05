package com.example.team30

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.team30.databinding.ActivityFeedBinding

class FeedActivity:AppCompatActivity() {
    lateinit var binding: ActivityFeedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}