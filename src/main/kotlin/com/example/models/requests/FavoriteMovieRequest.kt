package com.example.models.requests

import com.example.models.regular.MovieItem

data class FavoriteMovieRequest(
    val accountUsername: String,
    val movieEntity: MovieItem
)
