package com.example.database

import com.example.models.regular.MovieItem
import com.example.models.regular.Review
import com.example.models.regular.User
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val client = KMongo.createClient().coroutine
val database = client.getDatabase("TrecherDatabase")

val users = database.getCollection<User>("users")
val movies = database.getCollection<MovieItem>("movies")
val reviews = database.getCollection<Review>("reviews")