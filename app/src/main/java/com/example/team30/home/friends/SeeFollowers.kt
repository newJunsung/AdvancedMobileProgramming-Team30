package com.example.team30.home.friends

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.team30.R
import com.example.team30.home.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.item_follow.view.*

class SeeFollowers : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    lateinit var auth: FirebaseAuth

    companion object {
        const val TAG: String = "로그>>>"

        fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_followers)
        Log.d(TAG, "팔로워")

        db = Firebase.firestore
        storage = Firebase.storage
        auth = Firebase.auth
    }

    inner class SeeFollowersRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var followersUidList : ArrayList<String> = arrayListOf()

        init {
            db.collection("users")
                .document(auth.currentUser?.uid!!)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    Log.d(FriendsFragment.TAG, querySnapshot.toString())
                    val tempUID = querySnapshot!!.get("followers").toString().split(", ")
                    tempUID.forEach {
                        val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                        followersUidList.add(UID)
                    }
                    Log.d(FriendsFragment.TAG, followersUidList.toString())
                    notifyDataSetChanged() // 값 새로고침
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기 (팔로워 uid 만)
            Log.d(FriendsFragment.TAG, followersUidList.size.toString())
            Log.d(FriendsFragment.TAG, followersUidList[0])

            var instance = db.collection("profileImages")
                .document(followersUidList[position]).get()
            instance.addOnCompleteListener {
                if (it.isSuccessful) {
                    var url = it.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                        .into(view.item_follow_profile_imageview)
                    val name = it.result!!["name"]
                    val email = it.result!!["email"]
                    view.item_follow_profile_textview.text = "${name}(${email})"

                    view.item_follow_profile_imageview.setOnClickListener {
                        var userid = ""
                        db.collection("users")
                            .document(followersUidList[position])
                            .get()
                            .addOnSuccessListener { result ->
                                userid = result.toString()
                            }

                        var fragment = ProfileFragment.newInstance()
                        var bundle = Bundle()
                        bundle.putString("destinationUid", followersUidList[position])
                        bundle.putString("userID", userid)
                        fragment.arguments = bundle

                    }
                }
            }
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }
    }
}