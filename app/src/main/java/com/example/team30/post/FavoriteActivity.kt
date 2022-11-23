package com.example.team30.post

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.team30.R
import com.example.team30.home.feeds.FeedsFragment
import com.example.team30.home.friends.FriendsFragment
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.item_follow.view.*

class FavoriteActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    private var postUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        db = Firebase.firestore

        favorite_recyclerview.adapter = FavoriteRecyclerViewAdapter()
        favorite_recyclerview.layoutManager = LinearLayoutManager(this)

        postUid = intent.getStringExtra("contentUid") // '좋아요 수' 클릭해서 해당 포스트 uid 넘겨받고
        Log.d("postUid ", postUid!!)
    }

    inner class FavoriteRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var favoriteUidList: ArrayList<String> = arrayListOf()

        init {
            postUid = intent.getStringExtra("contentUid")
            db.collection("posts")
                .document(postUid!!)
                .addSnapshotListener { querySnapshot, _ ->
                    favoriteUidList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    Log.d("querySnapshot ", querySnapshot.get("favorites").toString())
                    val favoritesUid = querySnapshot.get("favorites").toString().split(", ") // 좋아요 한 사람 uid 가져오고
                    favoritesUid.forEach {
                        val Uid = (it.replace("[{}]".toRegex(), "").split("="))[0]
                        favoriteUidList.add(Uid)

                    }
                    Log.d("favoriteUidList ", favoriteUidList.toString())
                    notifyDataSetChanged() // 값 새로고침
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_follow, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기 (좋아요 한 uid 만)
            Log.d("favoriteUidList.size() ", favoriteUidList.size.toString())
            Log.d("favoriteUidList[0] ", favoriteUidList[0])
            if(favoriteUidList[0].isBlank()) {
                Log.d(FriendsFragment.TAG, "없다.")
                view.item_follow_profile_textview.text = "아직 하트를 누른 사람이 없습니다."
            } else {
                var instance = FirebaseFirestore.getInstance().collection("profileImages")
                    .document(favoriteUidList[position]).get()
                instance.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.item_follow_profile_imageview)
                        val name = task.result!!["name"]
                        val email = task.result!!["email"]
                        view.item_follow_profile_textview.text = "${name}(${email})"
                    }
                }
            }

        }

        override fun getItemCount(): Int {
            return favoriteUidList.size
        }
    }
}