package com.hamric.feature.details.data.repository

import com.hamric.core.network.api.TmdbApi
import com.hamric.feature.details.utils.CoroutineTestRule
import com.hamric.feature.details.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailRepositoryImplTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: MovieDetailRepositoryImpl
    private val mockApi: TmdbApi = mock()
    private val movieId = 123

    @Before
    fun setup() {
        repository = MovieDetailRepositoryImpl(mockApi)
    }

    @Test
    fun `getMovieDetails should return movie when API call is successful`() = runTest {
        val mockResponse = TestDataFactory.createMovieResponse(id = movieId, title = "Test Movie")
        whenever(mockApi.getMovieDetails(movieId)).thenReturn(mockResponse)

        val result = repository.getMovieDetails(movieId)

        assertThat(result.isSuccess).isTrue()
        val movie = result.getOrNull()
        assertThat(movie).isNotNull()
        assertThat(movie?.id).isEqualTo(movieId)
        assertThat(movie?.title).isEqualTo("Test Movie")
    }

    @Test
    fun `getMovieDetails should return failure when API throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockApi.getMovieDetails(movieId)).thenThrow(exception)

        val result = repository.getMovieDetails(movieId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `getMovieReviews should return PagingData when API call is successful`() = runTest {
        val mockReviews = listOf(
            TestDataFactory.createReviewResponse(id = "1", author = "User 1"),
            TestDataFactory.createReviewResponse(id = "2", author = "User 2")
        )
        val mockResponse = TestDataFactory.createReviewsResponse(
            page = 1,
            results = mockReviews,
            totalPages = 2,
            totalResults = 4
        )
        whenever(mockApi.getMovieReviews(movieId = movieId, page = 1)).thenReturn(mockResponse)

        val result = repository.getMovieReviews(movieId)
        val pagingData = result.first()
        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getMovieReviews should handle empty response`() = runTest {
        val mockResponse = TestDataFactory.createReviewsResponse(
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )
        whenever(mockApi.getMovieReviews(movieId = movieId, page = 1)).thenReturn(mockResponse)

        val result = repository.getMovieReviews(movieId)
        val pagingData = result.first()

        assertThat(pagingData).isNotNull()
    }

    @Test
    fun `getMovieTrailer should return trailer when available`() = runTest {
        val mockTrailer = TestDataFactory.createVideoResponse(
            key = "abc123",
            name = "Official Trailer",
            site = "YouTube",
            type = "Trailer"
        )
        val mockResponse = TestDataFactory.createVideosResponse(results = listOf(mockTrailer))
        whenever(mockApi.getMovieVideos(movieId)).thenReturn(mockResponse)

        val result = repository.getMovieTrailer(movieId)

        assertThat(result.isSuccess).isTrue()
        val trailer = result.getOrNull()
        assertThat(trailer).isNotNull()
        assertThat(trailer?.key).isEqualTo("abc123")
    }

    @Test
    fun `getMovieTrailer should return null when no trailer available`() = runTest {
        val mockResponse = TestDataFactory.createVideosResponse(results = emptyList())
        whenever(mockApi.getMovieVideos(movieId)).thenReturn(mockResponse)

        val result = repository.getMovieTrailer(movieId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `getMovieTrailer should return failure when API throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockApi.getMovieVideos(movieId)).thenThrow(exception)

        val result = repository.getMovieTrailer(movieId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}