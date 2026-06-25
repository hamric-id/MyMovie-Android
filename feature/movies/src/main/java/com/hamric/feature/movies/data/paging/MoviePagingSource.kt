package com.hamric.feature.movies.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hamric.core.model.Movie
import com.hamric.core.network.api.TmdbApi
import com.hamric.core.network.mapper.toDomainModels

class MoviePagingSource(
    private val api: TmdbApi,
    private val genreId: Int
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val page = params.key ?: 1
            val response = api.getMoviesByGenre(
                genreId = genreId,
                page = page.toInt()
            )

            LoadResult.Page(
                data = response.results.toDomainModels(),
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (page < response.totalPages.toInt()) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

