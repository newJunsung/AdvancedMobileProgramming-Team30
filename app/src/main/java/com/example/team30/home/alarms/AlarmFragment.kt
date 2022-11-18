package com.example.team30.home.alarms

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
import com.example.team30.home.profile.ProfileFragment
import com.example.team30.post.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_follow.view.*

class AlarmFragment : Fragment() {
    companion object {
        const val TAG: String = "로그"

        fun newInstance(): AlarmFragment {
            return AlarmFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(ProfileFragment.TAG, "알람 창이다.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter() // 어댑터와 리사이클러뷰 연결
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity) // 세로로 배치
        return view
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // 알람을 저장하는 리스트
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid) // 나에게 도착한 메시지만 필터링
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOList.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents) {
                        alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                        //Log.d("alarmDTOList : ", alarmDTOList[0].toString())
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)
            return CustomerViewHolder(view)
        }

        inner class CustomerViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            // db 에서 프로필 이미지 가져오기
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
//                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.item_comment_profile_imageview)
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.item_follow_profile_imageview)
                    }
                }

            // 종류에 따라 메시지 다르게 표시
            when(alarmDTOList[position].kind) {
                0 -> { // '좋아요'
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite) // '좋아요' 눌렀다는 코멘트 달아준다.
                    view.item_follow_profile_textview.text = str_0
//                    view.item_comment_profile_textview.text = str_0
                }
                1 -> { // 댓글
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_comment) // 댓글 달렸다는 코멘트 달아준다.
                    view.item_follow_profile_textview.text = str_0
//                    view.item_comment_profile_textview.text = str_0
                }
                2 -> { // 팔로우
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_follow) // 팔로우 했다는 코멘트 달아준다.
                    view.item_follow_profile_textview.text = str_0
//                    view.item_comment_profile_textview.text = str_0
                }
            }
            // view.item_comment_message_textview.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }
}