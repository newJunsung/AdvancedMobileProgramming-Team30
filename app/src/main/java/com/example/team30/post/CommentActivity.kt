package com.example.team30.post

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.team30.R
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_comment.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {
    var postUid: String? = null

    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        db = Firebase.firestore

        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        postUid = intent.getStringExtra("contentUid")

        comment_send_btn.setOnClickListener {
            var comment = PostDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_message_edittext.text.toString()
            comment.timestamp = System.currentTimeMillis()

            db.collection("posts").document(postUid!!).collection("comments").document().set(comment)

            comment_message_edittext.setText("")
        }
    }

    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<PostDTO.Comment> = arrayListOf()

        init {
            postUid = intent.getStringExtra("contentUid")
            db.collection("posts")
                .document(postUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, _ ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        comments.add(snapshot.toObject(PostDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.item_comment_message_textview.text = comments[position].comment
            view.item_comment_profile_textview.text = comments[position].userId
            db.collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        var url = it.result["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.item_comment_profile_imageview)
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}