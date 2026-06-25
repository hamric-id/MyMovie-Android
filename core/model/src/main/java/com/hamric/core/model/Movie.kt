package com.hamric.core.model

data class Movie(
    val id: UInt,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Float,
    val voteCount: UShort,
    val genreIds: List<UInt> = emptyList()
)