package com.hamric.feature.genres.data.repository

import com.hamric.core.model.Genre
import com.hamric.core.network.api.TmdbApi
import com.hamric.core.network.mapper.toDomainModels
import com.hamric.feature.genres.domain.repository.GenreRepository
import javax.inject.Inject

class GenreRepositoryImpl @Inject constructor(
    private val api: TmdbApi
) : GenreRepository {

    override suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val response = api.getGenres()
            val genres = response.genres.toDomainModels()
            Result.success(genres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}