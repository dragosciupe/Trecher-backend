package com.example.models.requests

data class ReviewRequest(
    val accountUsername: String,
    val movieId: Int,
    val reviewRating: Int,
    val reviewMessage: String
)
