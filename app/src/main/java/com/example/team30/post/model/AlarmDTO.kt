package com.example.team30.post.model

data class AlarmDTO (
    var destinationUid: String? = null,
    var userId: String? = null,
    var uid: String? = null,
    var kind: Int? = null, // 어떤 타입의 메시지인지 (메시지 종류) -> 0:like, 1:comment, 2:follow
    var message: String? = null,
    var timestamp: Long? = null
)