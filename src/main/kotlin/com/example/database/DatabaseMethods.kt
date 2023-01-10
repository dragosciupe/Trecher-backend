package com.example.database

import com.example.models.regular.MovieItem
import com.example.models.regular.Review
import com.example.models.regular.User
import com.example.models.requests.ReviewRequest
import com.example.models.responses.ReviewResponse
import com.example.security.checkHashForPassword
import org.litote.kmongo.*
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*

suspend fun findUser(username: String): User? {
    return users.findOne(User::username eq username)
}

suspend fun findUserById(userId: String): User? {
    return users.findOneById(userId)
}

suspend fun getUserId(username: String): String? {
    val user = findUser(username)
    return user?.userId
}

suspend fun findMovie(movieId: Int): MovieItem? {
    return movies.findOne(MovieItem::id eq movieId)
}

suspend fun checkIfUserExists(username: String): Boolean {
    return findUser(username) != null
}

suspend fun checkIfMovieExists(movieId: Int): Boolean {
    return findMovie(movieId) != null
}

suspend fun addUser(
    username: String,
    password: String,
    favoriteMoviesIds: List<Int> = listOf()
): Boolean {
    val userID = UUID.randomUUID().toString()
    val userToAdd = User(userID, username, password, favoriteMoviesIds)

    return users.insertOne(userToAdd).wasAcknowledged()
}

suspend fun saveMovie(movieEntity: MovieItem) {
    if(findMovie(movieEntity.id) == null) {
        movies.insertOne(movieEntity)
    }
}


suspend fun addMovieToFavorites(accountUsername: String, movieId: Int) {
    val user = findUser(accountUsername)!!
    val modifiedFavoriteMovies = user.favoriteMovies.toMutableList().apply {
        add(movieId)
    }
    val modifiedUser = user.copy(favoriteMovies = modifiedFavoriteMovies)

    users.updateOneById(user.userId, modifiedUser)
}

suspend fun createReviewFromRequest(reviewRequest: ReviewRequest): Boolean {
    if(!checkIfUserExists(reviewRequest.accountUsername)) return false

    if(reviewRequest.reviewRating < 1 || reviewRequest.reviewRating > 5) return false

    val newReview = Review(
        UUID.randomUUID().toString(),
        getUserId(reviewRequest.accountUsername)!!,
        reviewRequest.movieId,
        reviewRequest.reviewRating,
        reviewRequest.reviewMessage,
        System.currentTimeMillis()
    )

    return reviews.insertOne(newReview).wasAcknowledged()
}



suspend fun removeMovieFromFavorites(accountUsername: String, movieId: Int) {
    val user = findUser(accountUsername)!!
    val modifiedFavoriteMovies = user.favoriteMovies.toMutableList().apply {
        remove(movieId)
    }
    val modifiedUser = user.copy(favoriteMovies = modifiedFavoriteMovies)

    users.updateOneById(user.userId, modifiedUser)
}

suspend fun loginUser(username: String, password: String): Boolean {
    val userToFind = findUser(username)!!
    val hashedUserPassword = userToFind.password
    return checkHashForPassword(hashedUserPassword, password)
}

suspend fun getFavoriteMovies(accountUsername: String): List<MovieItem> {
    val savedMoviesIds =  findUser(accountUsername)?.favoriteMovies ?: return listOf()
    val savedMoviesList = mutableListOf<MovieItem>()
    savedMoviesIds.forEach { movieId ->
        movies.findOne(MovieItem::id eq movieId)?.also {
            savedMoviesList.add(it);
        }
    }
    return savedMoviesList
}

suspend fun getAllReviewsForMovie(movieId: Int): List<ReviewResponse> {
    val reviewResponseList = mutableListOf<ReviewResponse>()
    val reviews = reviews.find(Review::movieId eq movieId).toList()

    reviews.forEach { review ->
        reviewResponseList.add(
            ReviewResponse(
                findUserById(review.userId)!!.username,
                review.movieId,
                review.rating,
                review.message,
                review.timestamp
            )
        )
    }
    return reviewResponseList
}