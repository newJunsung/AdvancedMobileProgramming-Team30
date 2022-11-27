package com.example.team30.home.followers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.team30.R
import com.example.team30.home.friends.FriendsFragment
import com.example.team30.home.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_seefollowers.*
import kotlinx.android.synthetic.main.fragment_seefollowers.view.*
import kotlinx.android.synthetic.main.item_follow.view.*

class SeeFollowersFragment : Fragment() {
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    lateinit var auth: FirebaseAuth

    companion object {
        const val TAG: String = "로그"

        fun newInstance(): SeeFollowersFragment {
            return SeeFollowersFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(ProfileFragment.TAG, "팔로워")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = Firebase.firestore
        storage = Firebase.storage
        auth = Firebase.auth

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_seefollowers, container, false)
        firestore = FirebaseFirestore.getInstance()

        view.seefollowers_recyclerview.adapter = FollowersRecyclerviewAdapter()
        view.seefollowers_recyclerview.layoutManager = LinearLayoutManager(activity)

        view.findViewById<SearchView>(R.id.search_friends)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    (seefollowers_recyclerview.adapter as SeeFollowersFragment.FollowersRecyclerviewAdapter)
                        .searchFriends(p0!!)
                    return true
                }

            })

        return view
    }

    inner class FollowersRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var followerUidList: ArrayList<String> = arrayListOf()

        // db에 접근해서 followerUidList 세팅
        init {
            firestore.collection("users")
                .document(auth.currentUser?.uid!!)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    Log.d(FriendsFragment.TAG, querySnapshot.toString())
                    val tempUID = querySnapshot!!.get("followers").toString().split(", ")
                    tempUID.forEach {
                        val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                        followerUidList.add(UID)
                    }
                    Log.d(FriendsFragment.TAG, followerUidList.toString())
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

            // db 에서 프로필 이미지 가져오기 (팔로우 하는 uid 만)
            Log.d(FriendsFragment.TAG, followerUidList.size.toString())
            if (followerUidList[0] != "") {
                var instance = firestore.collection("profileImages")
                    .document(followerUidList[position]).get()
                instance.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.item_follow_profile_imageview)
                        val name = task.result!!["name"]
                        val email = task.result!!["email"]
                        view.item_follow_profile_textview.text = "${name}(${email})"

                        view.item_follow_profile_imageview.setOnClickListener {
                            var userid = ""
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(followerUidList[position])
                                .get()
                                .addOnSuccessListener { result ->
                                    userid = result.toString()
                                }

                            var fragment = ProfileFragment.newInstance()
                            var bundle = Bundle()
                            bundle.putString("destinationUid", followerUidList[position])
                            bundle.putString("userID", userid)
                            fragment.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.fragments_frame, fragment)?.commit()
                        }
                    }
                }
            } else {
                view.item_follow_profile_textview.text = "팔로워인 친구가 없어요..."
            }
        }

        override fun getItemCount(): Int {
            return followerUidList.size
        }

        // 파이어스토어에서 데이터를 불러와서 검색어가 있는지 판단
        fun searchFriends(search: String) {

            for (followerUid in followerUidList) {
                FirebaseFirestore.getInstance().collection("profileImages").document(followerUid)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        Log.d("검색한 following ", followerUid)
                        Log.d("querysnapshot ", querySnapshot.toString())
                        // followerUid 에 대한 name 값을 arraylist 로
                        val nameList: ArrayList<String> = arrayListOf()
                        //(it.replace("[{}]".toRegex(), "").split("="))[0]
                        Log.d("uid ", querySnapshot?.getString("name").toString())
                        if (querySnapshot?.getString("name").toString().contains(search)) {
                            followerUidList.clear()
                            followerUidList.add(followerUid)
                            Log.d("검색한 후 followersUidList ", followerUidList.toString())
                        }
                        notifyDataSetChanged()
                    }
            }

            if (search.isEmpty()) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        Log.d(TAG, querySnapshot.toString())
                        followerUidList.clear()
                        val tempUID = querySnapshot!!.get("followings").toString().split(", ")
                        tempUID.forEach {
                            val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                            followerUidList.add(UID)
                        }
                        Log.d("맨 처음 followersUidList 세팅 ", followerUidList.toString())
                        notifyDataSetChanged() // 값 새로고침
                    }

                Log.d("검색한 후 followersUidList ", followerUidList.toString())
            }
        }
    }
}