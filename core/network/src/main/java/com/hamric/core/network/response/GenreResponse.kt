package com.hamric.core.network.response

import com.google.gson.annotations.SerializedName

data class GenreResponse(
    @SerializedName("id")
    val id: UInt,
    @SerializedName("name")
    val name: String
)

data class GenresResponse(
    @SerializedName("genres")
    val genres: List<GenreResponse>
)