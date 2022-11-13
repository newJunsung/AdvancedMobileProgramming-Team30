package com.example.team30.home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
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
import com.example.team30.post.model.PostDTO
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
        if (currentUserUid == uid) {
            fragmentView?.logout_follow_button?.text = "LOG OUT"
            fragmentView?.logout_follow_button?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            fragmentView?.logout_follow_button?.text = "FOLLOW"
            var snsActivity = (activity as SNSActivity)
            snsActivity.toolbar_logo?.visibility = View.GONE
            snsActivity.toolbar_user_id?.visibility = View.VISIBLE
            snsActivity.toolbar_user_id?.text = arguments?.getString("userID")
            snsActivity.toolbar_back_button?.visibility = View.VISIBLE
            snsActivity.toolbar_back_button?.setOnClickListener {
                snsActivity.bottom_tab_bar.selectedItemId = R.id.tab_bar_feeds
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
        return fragmentView
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