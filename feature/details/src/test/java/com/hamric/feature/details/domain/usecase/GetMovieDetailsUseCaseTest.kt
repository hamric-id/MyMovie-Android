package com.hamric.feature.details.domain.usecase

import com.hamric.feature.details.domain.repository.MovieDetailRepository
import com.hamric.feature.details.utils.CoroutineTestRule
import com.hamric.feature.details.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class GetMovieDetailsUseCaseTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: GetMovieDetailsUseCase
    private val mockRepository: MovieDetailRepository = mock()
    private val movieId = 123

    @Before
    fun setup() {
        useCase = GetMovieDetailsUseCase(mockRepository)
    }

    @Test
    fun `invoke should return movie details when repository returns success`() = runTest {
        val expectedMovie = TestDataFactory.createMovie(id = movieId)
        whenever(mockRepository.getMovieDetails(movieId)).thenReturn(Result.success(expectedMovie))

        val result = useCase(movieId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedMovie)
        verify(mockRepository).getMovieDetails(movieId)
    }

    @Test
    fun `invoke should return failure when repository returns error`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockRepository.getMovieDetails(movieId)).thenReturn(Result.failure(exception))

        val result = useCase(movieId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        verify(mockRepository).getMovieDetails(movieId)
    }
}