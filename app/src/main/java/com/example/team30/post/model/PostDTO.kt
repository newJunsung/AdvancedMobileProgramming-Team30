package com.example.team30.post.model

data class PostDTO(
    var title: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: Map<String, Boolean> = HashMap()
) {
    data class Comment(
        var userId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}