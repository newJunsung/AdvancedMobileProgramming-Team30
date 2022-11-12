package com.example.team30.home.feeds

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.team30.R
import com.example.team30.databinding.FragmentFeedsBinding
import com.example.team30.post.model.PostDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FeedsFragment: Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    companion object {
        const val TAG: String = "로그"

        fun newInstance(): FeedsFragment {
            return FeedsFragment()
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
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_feeds, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.findViewById<RecyclerView>(R.id.feeds_recyclerview).adapter = FeedsRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.feeds_recyclerview).layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class FeedsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var PostDTOs : ArrayList<PostDTO> = arrayListOf() // PostDTO 담아두는 arraylist
        var PostUidList : ArrayList<String> = arrayListOf() // uid 담아두는 arraylist

        // db에 접근해서 시간순으로 데이터 받아올 수 있는 쿼리
        init {
            firestore?.collection("posts")?.orderBy("timestamp")?.addSnapshotListener {querySnapshot, firebaseFirestoreException ->
                // 받자마자 값 초기화하고
                PostDTOs.clear()
                PostUidList.clear()
                // snapshot 에 넘어오는 데이터 읽기
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(PostDTO::class.java)
                    if (item != null) {
                        PostDTOs.add(item)
                    }
                    PostUidList.add(snapshot.id)
                }
                notifyDataSetChanged() // 값 새로고침
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.feed_timeline, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        // 서버에서 넘어온 데이터 매핑
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            // UserId
            viewholder.findViewById<TextView>(R.id.feed_timeline_profile_textview).text = PostDTOs[position].userId

            // Image
            Glide.with(holder.itemView.context).load(PostDTOs[position].imageUrl)
                .into(viewholder.findViewById(R.id.feed_timeline_imageview_content))

            // Explain of content
            viewholder.findViewById<TextView>(R.id.feed_timeline_description_textview).text = PostDTOs!![position].description

            // likes
            viewholder.findViewById<TextView>(R.id.feed_timeline_favoritecounter_textview).text = "Likes " + PostDTOs!![position].favoriteCount

            // ProfileImage
            Glide.with(holder.itemView.context).load(PostDTOs[position].imageUrl)
                .into(viewholder.findViewById(R.id.feed_timeline_profile_image))

            // '좋아요' 버튼 클릭 이벤트
            viewholder.findViewById<ImageView>(R.id.feed_timeline_favorite_imageview).setOnClickListener {
                favoriteClickEvent(position)
            }

            if (PostDTOs!![position].favorites.containsKey(uid)) { // 클릭한 경우 하트 이미지
                viewholder.findViewById<ImageView>(R.id.feed_timeline_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite)
            } else { // 클릭하지 않은 경우 비어 있는 하트
                viewholder.findViewById<ImageView>(R.id.feed_timeline_favorite_imageview)
                    .setImageResource(R.drawable.ic_favorite_border)
            }
        }

        // '좋아요' 클릭하면 카운트를 다시 세팅
        fun favoriteClickEvent(position: Int) {
            // 선택한 컨텐츠의 uid 를 받아와서 넣어준다.
            var doc = firestore?.collection("posts")?.document(PostUidList[position])
            firestore?.runTransaction { transaction ->
                var postDTO = transaction.get(doc!!).toObject(PostDTO::class.java)

                // '좋아요' 가 이미 클릭되어 있는 경우 / 아닌 경우
                if(postDTO!!.favorites.containsKey(uid)) { // 클릭되어 있는 경우 (클릭 해제하면 됨)
                    postDTO?.favoriteCount = postDTO.favoriteCount - 1
                    postDTO?.favorites?.remove(uid)
                } else { // 클릭되어 있지 않은 경우
                    postDTO?.favoriteCount = postDTO.favoriteCount + 1
                    postDTO.favorites[uid!!] = true // favorites 에 uid 값 추가
                }
                transaction.set(doc, postDTO) // transaction 을 서버로 돌려준다.
            }
        }

        override fun getItemCount(): Int {
            return PostDTOs.size // 리사이클러뷰 개수 넘겨주기
        }
    }
}