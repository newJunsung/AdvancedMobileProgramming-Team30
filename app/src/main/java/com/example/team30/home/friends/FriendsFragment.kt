package com.example.team30.home.friends

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
import com.example.team30.home.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.android.synthetic.main.item_follow.*
import kotlinx.android.synthetic.main.item_follow.view.*

class FriendsFragment: Fragment() {
    var uid: String? = null
    var followUidList: ArrayList<String> = arrayListOf() // follower uid 담아두는 arraylist

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
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_friends, container, false)
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.friendsfragment_recyclerview.adapter = FriendsRecyclerviewAdapter()
        view.friendsfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        view.findViewById<SearchView>(R.id.search_friends)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    (friendsfragment_recyclerview.adapter as FriendsRecyclerviewAdapter)
                        .searchFriends(p0!!)
                    return true
                }

            })

        return view
    }

    inner class FriendsRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // db에 접근해서 followUidList 세팅
        init {
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Log.d(TAG, querySnapshot.toString())
                    val tempUID = querySnapshot!!.get("followings").toString().split(", ")
                    tempUID.forEach {
                        val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                        followUidList.add(UID)
                    }
                    Log.d("맨 처음 followUidList 세팅 ", followUidList.toString())
                    notifyDataSetChanged() // 값 새로고침
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return CustomerViewHolder(view)
        }

        inner class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기 (팔로우 하는 uid 만)
            Log.d(TAG, followUidList.size.toString())
            Log.d(TAG, followUidList[0])

            if (followUidList[0].isBlank()) {
                Log.d(TAG, "없다.")
                view.item_follow_profile_textview.text = "팔로우하고 있는 친구가 없어요..."
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

                        view.item_follow_profile_imageview.setOnClickListener {
                            var userid = ""
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(followUidList[position])
                                .get()
                                .addOnSuccessListener { result ->
                                    userid = result.toString()
                                }

                            var fragment = ProfileFragment.newInstance()
                            var bundle = Bundle()
                            bundle.putString("destinationUid", followUidList[position])
                            bundle.putString("userID", userid)
                            fragment.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.fragments_frame, fragment)?.commit()
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return followUidList.size
        }

        // 파이어스토어에서 데이터를 불러와서 검색어가 있는지 판단
        fun searchFriends(search: String) {

            for (followUid in followUidList) {
                FirebaseFirestore.getInstance().collection("profileImages").document(followUid)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        Log.d("검색한 following ", followUid)
                        Log.d("querysnapshot ", querySnapshot.toString())
                        // followUid 에 대한 name 값을 arraylist 로
                        val nameList: ArrayList<String> = arrayListOf()
                        //(it.replace("[{}]".toRegex(), "").split("="))[0]
                        Log.d("uid ", querySnapshot?.getString("name").toString())
                        if (querySnapshot?.getString("name").toString().contains(search)) {
                            followUidList.clear()
                            followUidList.add(followUid)
                            Log.d("검색한 후 followUidList ", followUidList.toString())
                        }
                        notifyDataSetChanged()
                    }
            }

            if (search.isEmpty()) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        //Log.d(TAG, querySnapshot.toString())
                        followUidList.clear()
                        val tempUID = querySnapshot!!.get("followings").toString().split(", ")
                        tempUID.forEach {
                            val UID = (it.replace("[{}]".toRegex(), "").split("="))[0]
                            followUidList.add(UID)
                        }
                        Log.d("맨 처음 followUidList 세팅 ", followUidList.toString())
                        notifyDataSetChanged() // 값 새로고침
                    }

                Log.d("검색한 후 followUidList ", followUidList.toString())
            }
        }
    }
}