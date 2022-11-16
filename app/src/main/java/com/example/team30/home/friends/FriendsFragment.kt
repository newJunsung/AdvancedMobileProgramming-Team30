package com.example.team30.home.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.team30.R
import com.example.team30.home.feeds.FeedsFragment
import com.example.team30.post.model.FollowDTO
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.android.synthetic.main.item_follow.view.*

class FriendsFragment: Fragment() {
    var uid: String? = null

    companion object {
        const val TAG: String = "로그"

        fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "프로필 창이다.")
    }

    // fragment와 layout을 연결
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_friends, container, false)
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.friendsfragment_recyclerview.adapter = FriendsRecyclerviewAdapter()
        view.friendsfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class FriendsRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var followDTOs : ArrayList<FollowDTO> = arrayListOf() // FollowDTO 담아두는 arraylist
        var followUidList : ArrayList<String> = arrayListOf() // follower uid 담아두는 arraylist

        // db에 접근해서 followUidList 세팅
        init {
            FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    followDTOs.clear()
                    followUidList.clear()
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(FollowDTO::class.java)
                        if (item != null) {
                            followDTOs.add(item)
                        }
                        followUidList.add(snapshot.id) // 일단 다 담고
                    }
                    notifyDataSetChanged() // 값 새로고침
            }

            // followers 가 null 인 uid 를 followUidList 에서 제거
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("followingCount", 0)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(FollowDTO::class.java)
                        if (item != null) {
                            followDTOs.remove(item) // follower 없는 데이터 제거
                       }
                        followUidList.remove(snapshot.id) // follower 없는 uid 제거
                    }
                    notifyDataSetChanged() // 값 새로고침
                }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return CustomerViewHolder(view)
        }

        inner class CustomerViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기 (팔로우 하는 uid 만)
            FirebaseFirestore.getInstance().collection("profileImages").document(followUidList[position]).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.item_follow_profile_imageview)
                    }
                }

            // followUidList 출력해보기
            for (item in followUidList) {
                Log.d("followUidList: ", item)
            }

            if (followDTOs!![position].followings.containsKey(uid)) { // 사용자를 팔로우하고 있으면
                view.item_follow_profile_textview.text = followDTOs[position].userId
            }
        }

        override fun getItemCount(): Int {
            return followUidList.size
        }

    }


}