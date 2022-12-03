package com.example.routes.http

import com.example.database.*
import com.example.models.requests.FavoriteMovieRequest
import com.example.models.requests.ReviewRequest
import com.example.models.responses.BasicApiResponse
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.getFavoriteMovies() {
    get("/getFavoriteMovies") {
        val username = call.parameters["accountUsername"]
        if(username == null) {
            call.respond(HttpStatusCode.BadRequest, "Bad request format")
            return@get
        }

        if(!checkIfUserExists(username)) {
            call.respond(HttpStatusCode.BadRequest, "The user does not exist")
            return@get
        }

        call.respond(OK, getFavoriteMovies(username))
    }
}

fun Route.addMovieToFavourites() {
    post("/addMovieToFavorites") {
        val request = call.receiveOrNull<FavoriteMovieRequest>()
        if(request == null) {
            call.respond(HttpStatusCode.BadRequest, BasicApiResponse(false, "Bad request format"))
            return@post
        }
        if(!checkIfUserExists(request.accountUsername)) {
            call.respond(HttpStatusCode.BadRequest, BasicApiResponse(false, "The username trying to add this job to favourites does not exist"))
            return@post
        }

        if(request.movieEntity.id in findUser(request.accountUsername)!!.favoriteMovies) {
            call.respond(HttpStatusCode.OK, BasicApiResponse(false, "You already saved this movie"))
        } else {
            saveMovie(request.movieEntity)
            addMovieToFavorites(request.accountUsername, request.movieEntity.id)
            call.respond(HttpStatusCode.OK, BasicApiResponse(true, "The movie has been saved successfully"))
        }
    }
}

fun Route.deleteMovieFromFavorites() {
    post("/deleteMovieFromFavorites") {
        val request = call.receiveNullable<FavoriteMovieRequest>()
        if(request == null) {
            call.respond(HttpStatusCode.BadRequest, BasicApiResponse(false, "Bad request format"))
            return@post
        }
        if(!checkIfUserExists(request.accountUsername)) {
            call.respond(HttpStatusCode.BadRequest, BasicApiResponse(false, "The username trying to add this movie to favourites does not exist"))
            return@post
        }

        if(request.movieEntity.id !in findUser(request.accountUsername)!!.favoriteMovies) {
            call.respond(HttpStatusCode.OK, BasicApiResponse(false, "You don't have this movie as a favorite"))
        } else {
            removeMovieFromFavorites(request.accountUsername, request.movieEntity.id)
            call.respond(HttpStatusCode.OK, BasicApiResponse(true, "The movie has been deleted successfully"))
        }
    }
}

fun Route.addMovieReview() {
    post("/addMovieReview") {
        val reviewRequest: ReviewRequest?

        try {
            reviewRequest = call.receive()
        } catch (e: Error) {
            call.respond(BadRequest, BasicApiResponse(false, "Bad request format"))
            return@post
        }

        println(reviewRequest.accountUsername)
        println(reviewRequest.movieId)
        println(reviewRequest.reviewRating)
        println(reviewRequest.reviewMessage)

        if (!checkIfUserExists(reviewRequest.accountUsername)) {
            call.respond(BadRequest, BasicApiResponse(false, "The user does not exist"))
            return@post
        }

        if (!checkIfMovieExists(reviewRequest.movieId)) {
            call.respond(BadRequest, BasicApiResponse(false, "The movie does not exist"))
            return@post
        }

        if (createReviewFromRequest(reviewRequest)) {
            call.respond(OK, BasicApiResponse(true, "Review added successfully"))
        } else {
            call.respond(OK, BasicApiResponse(false, "Unknown server error"))
        }
    }
}

fun Route.getReviewsForMovie() {
    get("/getAllReviewsForMovie") {
        val movieId = call.parameters["movieId"]
        if(movieId.isNullOrEmpty()) {
            call.respond(BadRequest, BasicApiResponse(false, "Bad request"))
            return@get
        }

        if(!checkIfMovieExists(movieId.toInt())) {
            call.respond(OK, BasicApiResponse(false, "Movie does not exist"))
            return@get
        }

        call.respond(getAllReviewsForMovie(movieId.toInt()))
    }
}
