package com.example.team30.home.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.team30.R
import com.example.team30.home.SNSActivity
import com.example.team30.login.LoginActivity
import com.example.team30.post.model.AlarmDTO
import com.example.team30.post.model.FollowDTO
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sns.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment: Fragment() {
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null

    companion object {
        const val TAG: String = "프로필"
        var PICK_PROFILE_FROM_ALBUM = 10

        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "프로필 창이다.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_profile, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 자신의 프로필로 들어오면 로그아웃 할 수 있도록, 상대의 프로필로 들어오면 팔로우를 누를 수 있음.
        uid = arguments?.getString("destinationUid")
        currentUserUid = auth?.currentUser?.uid
        if (currentUserUid == uid) { //자신의 프로필
            fragmentView?.logout_follow_button?.text = "LOG OUT"
            fragmentView?.logout_follow_button?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {//상대의 프로필
            fragmentView?.logout_follow_button?.text = "FOLLOW"
            var snsActivity = (activity as SNSActivity)
            snsActivity.toolbar_logo?.visibility = View.GONE
            snsActivity.toolbar_user_id?.visibility = View.VISIBLE
            snsActivity.toolbar_user_id?.text = arguments?.getString("userID")
            snsActivity.toolbar_back_button?.visibility = View.VISIBLE
            snsActivity.toolbar_back_button?.setOnClickListener {
                snsActivity.bottom_tab_bar.selectedItemId = R.id.tab_bar_feeds
            }
            fragmentView?.logout_follow_button?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.user_recyclerview?.adapter = ProfileRecyclerViewAdapter()
        fragmentView?.user_recyclerview?.layoutManager = GridLayoutManager(activity, 3)
        fragmentView?.user_profile?.setOnClickListener { // 프로필 사진을 누르면 프로필 사진을 수정 가능.
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    // 팔로우하는 기능
    fun requestFollow() {
        // 내가 팔로잉 당할때
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)!!
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true
                transaction.set(tsDocFollowing,followDTO!!)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                followDTO?.followingCount = followDTO?.followingCount!!.minus(1)
                followDTO?.followings?.remove(uid)

            }else{
                followDTO?.followingCount = followDTO?.followingCount!!.plus(1)
                followDTO?.followings?.set(uid!!, true)
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        //내가 팔로우 할 때
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                followerAlarm(uid!!) // 최초로 누가 팔로우하면 알람이 간다.
                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }

            if(followDTO!!.followers.containsKey(currentUserUid!!)){
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)

            }else{
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true

                // 팔로우 카운트 올라가면 알람이 간다.
                followerAlarm(uid!!)
            }

            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    fun getFollowerAndFollowing(){
            firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(documentSnapshot == null) return@addSnapshotListener
                var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
                if(followDTO?.followingCount != null){
                    fragmentView?.user_following_count?.text = followDTO?.followingCount?.toString()
                }
                if(followDTO?.followerCount != null){
                    fragmentView?.user_follower_count?.text = followDTO?.followerCount?.toString()
                    if(followDTO?.followers?.containsKey(currentUserUid!!) == true){
                        fragmentView?.logout_follow_button?.text = "CANCEL"
                        fragmentView?.logout_follow_button?.background
                            ?.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.gray),PorterDuff.Mode.MULTIPLY)
                    }else{
                        if(uid != currentUserUid){
                            fragmentView?.logout_follow_button?.text = "FOLLOW"
                            fragmentView?.logout_follow_button?.background?.colorFilter = null
                        }

                    }
                }
        }
    }

    // 팔로우 알람 기능
    fun followerAlarm(destinationUid: String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    // firebase 내에 저장된 profileImages에서 사용자의 프로필 사진 가져오기
    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { value, error ->
            if(value == null) return@addSnapshotListener
            if(value.data != null) {
                var url = value?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.user_profile!!)
            }
        }
    }

    inner class ProfileRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var PostDTOs: ArrayList<PostDTO> = arrayListOf()

        init {
            firestore?.collection("posts")?.whereEqualTo("uid", uid)?.addSnapshotListener { value, error ->
                if (value == null) {
                    return@addSnapshotListener
                }

                Log.d(TAG, "${value.documents.size}")
                for (snapshot in value.documents) {
                    PostDTOs.add(snapshot.toObject(PostDTO::class.java)!!)
                }
                fragmentView?.user_post_count?.text = PostDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView): RecyclerView.ViewHolder(imageView) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(PostDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return PostDTOs.size
        }

    }
}