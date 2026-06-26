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
class GetMovieTrailerUseCaseTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: GetMovieTrailerUseCase
    private val mockRepository: MovieDetailRepository = mock()
    private val movieId = 123

    @Before
    fun setup() {
        useCase = GetMovieTrailerUseCase(mockRepository)
    }

    @Test
    fun `invoke should return trailer when repository returns success`() = runTest {
        val expectedTrailer = TestDataFactory.createVideo()
        whenever(mockRepository.getMovieTrailer(movieId)).thenReturn(Result.success(expectedTrailer))

        val result = useCase(movieId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedTrailer)
        verify(mockRepository).getMovieTrailer(movieId)
    }

    @Test
    fun `invoke should return null when no trailer available`() = runTest {
        whenever(mockRepository.getMovieTrailer(movieId)).thenReturn(Result.success(null))

        val result = useCase(movieId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
        verify(mockRepository).getMovieTrailer(movieId)
    }

    @Test
    fun `invoke should return failure when repository returns error`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockRepository.getMovieTrailer(movieId)).thenReturn(Result.failure(exception))

        val result = useCase(movieId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
        verify(mockRepository).getMovieTrailer(movieId)
    }
}