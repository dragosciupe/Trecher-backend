package com.example.models.regular

import org.bson.codecs.pojo.annotations.BsonId
import java.io.Serializable

data class User(
    @BsonId
    val userId: String,
    val username: String,
    val password: String,
    val favoriteMovies: List<Int>
)
