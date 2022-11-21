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
import com.example.team30.post.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.android.synthetic.main.item_follow.view.*

class FriendsFragment: Fragment() {
    var uid: String? = null

    companion object {
        const val TAG: String = "로그>>>"

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
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    Log.d(TAG, querySnapshot.toString())
//                    followDTOs.clear()
//                    followUidList.clear()
//                    for (snapshot in querySnapshot!!.documents) {
//                        Log.d(TAG, snapshot.toString())
//                        var item = snapshot.toObject(FollowDTO::class.java)
//                        if (item != null) {
//                            followDTOs.add(item)
//                        }
//                        //Log.d("followDTOs: ", followDTOs.get(0).toString())
//                        followUidList.add(snapshot.id) // 일단 다 담고
//                    }

                    val tempUID = querySnapshot!!.get("followings").toString().split(", ")
                    tempUID.forEach {
                        val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                        followUidList.add(UID)
                    }
                    Log.d(TAG, followUidList.toString())
                    notifyDataSetChanged() // 값 새로고침
            }

//            // followers 가 null 인 uid 를 followUidList 에서 제거
//            FirebaseFirestore.getInstance().collection("users").whereEqualTo("followCount", 0)
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    for (snapshot in querySnapshot!!.documents) {
//                        var item = snapshot.toObject(FollowDTO::class.java)
//                        if (item != null) {
//                            followDTOs.remove(item) // follower 없는 데이터 제거
//                       }
//                        followUidList.remove(snapshot.id) // follower 없는 uid 제거
//                    }
//                    notifyDataSetChanged() // 값 새로고침
//                }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return CustomerViewHolder(view)
        }

        inner class CustomerViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기 (팔로우 하는 uid 만)
            Log.d(TAG, followUidList.size.toString())
            Log.d(TAG, followUidList[0])
            if(followUidList[0].isBlank()) {
                Log.d(TAG, "없다.")
                view.item_follow_profile_textview.text = "친구가 없어요..."
            } else {
                var instance = FirebaseFirestore.getInstance().collection("profileImages")
                    .document(followUidList[position]).get()
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

//            // followUidList 출력해보기
//            for (item in followDTOs) {
//                Log.d("followUidList: ", item.userId.toString())
//            }

//            if(followDTOs!![position].followers.containsKey(uid)) { // 사용자를 팔로우하고 있으면
//                //view.item_follow_profile_textview.text = followDTOs[position].userId.toString() // 안됨..ㅠ
//                view.item_follow_profile_textview.text = followUidList[position]
//
//                Log.d("userId: ", followDTOs[position].userId.toString())
//            }
        }

        override fun getItemCount(): Int {
            return followUidList.size
        }

    }


}