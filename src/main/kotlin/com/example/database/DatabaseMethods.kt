package com.example.database

import com.example.models.regular.MovieItem
import com.example.models.regular.User
import com.example.security.checkHashForPassword
import org.litote.kmongo.*
import java.util.*

suspend fun findUser(username: String): User? {
    return users.findOne(User::username eq username)
}

suspend fun findMovie(movieId: Int): MovieItem? {
    return movies.findOne(MovieItem::id eq movieId)
}

suspend fun checkIfUserExists(username: String): Boolean {
    val userToFind = users.findOne(User::username eq username)
    return userToFind != null
}

suspend fun checkIfMovieExists(movieId: Int): Boolean {
    val movieToFind = findMovie(movieId)
    return movieToFind != null
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