package com.example.models.regular

import com.example.database.findUser
import com.example.database.findUserById
import com.example.models.responses.ReviewResponse
import org.bson.codecs.pojo.annotations.BsonId
import java.sql.Timestamp

data class Review(
    @BsonId
    val reviewId: String,
    val userId: String,
    val movieId: Int,
    val rating: Double,
    val message: String,
    val timestamp: Long
)
