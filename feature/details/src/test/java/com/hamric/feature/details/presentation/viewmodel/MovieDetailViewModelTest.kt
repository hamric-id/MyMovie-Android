package com.hamric.feature.details.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.hamric.feature.details.domain.usecase.GetMovieDetailsUseCase
import com.hamric.feature.details.domain.usecase.GetMovieReviewsUseCase
import com.hamric.feature.details.domain.usecase.GetMovieTrailerUseCase
import com.hamric.feature.details.presentation.state.MovieDetailUiState
import com.hamric.feature.details.utils.CoroutineTestRule
import com.hamric.feature.details.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: MovieDetailViewModel
    private val mockGetDetailsUseCase: GetMovieDetailsUseCase = mock()
    private val mockGetReviewsUseCase: GetMovieReviewsUseCase = mock()
    private val mockGetTrailerUseCase: GetMovieTrailerUseCase = mock()
    private val movieId = 123

    @Before
    fun setup() {
        viewModel = MovieDetailViewModel(
            getMovieDetailsUseCase = mockGetDetailsUseCase,
            getMovieReviewsUseCase = mockGetReviewsUseCase,
            getMovieTrailerUseCase = mockGetTrailerUseCase
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {

        assertThat(viewModel.uiState.value).isInstanceOf(MovieDetailUiState.Loading::class.java)
        assertThat(viewModel.reviews.value).isNull()
        assertThat(viewModel.isReviewsLoading.value).isFalse()
        assertThat(viewModel.reviewsError.value).isNull()
    }

    @Test
    fun `loadMovieDetails should update state to Success with movie and trailer`() = runTest {

        val expectedMovie = TestDataFactory.createMovie(id = movieId)
        val expectedTrailer = TestDataFactory.createVideo()
        whenever(mockGetDetailsUseCase(movieId)).thenReturn(Result.success(expectedMovie))
        whenever(mockGetTrailerUseCase(movieId)).thenReturn(Result.success(expectedTrailer))


        viewModel.loadMovieDetails(movieId)

        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(MovieDetailUiState.Success::class.java)
        val successState = state as MovieDetailUiState.Success
        assertThat(successState.movie).isEqualTo(expectedMovie)
        assertThat(successState.trailer).isEqualTo(expectedTrailer)
    }

    @Test
    fun `loadMovieDetails should update state to Success with movie but no trailer`() = runTest {

        val expectedMovie = TestDataFactory.createMovie(id = movieId)
        whenever(mockGetDetailsUseCase(movieId)).thenReturn(Result.success(expectedMovie))
        whenever(mockGetTrailerUseCase(movieId)).thenReturn(Result.success(null))


        viewModel.loadMovieDetails(movieId)
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(MovieDetailUiState.Success::class.java)
        val successState = state as MovieDetailUiState.Success
        assertThat(successState.movie).isEqualTo(expectedMovie)
        assertThat(successState.trailer).isNull()
    }

    @Test
    fun `loadMovieDetails should update state to Error when details fetch fails`() = runTest {

        val errorMessage = "Failed to load movie"
        val exception = RuntimeException(errorMessage)
        whenever(mockGetDetailsUseCase(movieId)).thenReturn(Result.failure(exception))


        viewModel.loadMovieDetails(movieId)
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(MovieDetailUiState.Error::class.java)
        val errorState = state as MovieDetailUiState.Error
        assertThat(errorState.message).contains(errorMessage)
    }

    @Test
    fun `loadReviews should update reviews Flow when successful`() = runTest {

        val expectedReviews = TestDataFactory.createReviewList(3)
        val pagingData = PagingData.from(expectedReviews)
        whenever(mockGetReviewsUseCase(movieId)).thenReturn(flowOf(pagingData))


        viewModel.loadReviews(movieId)
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        assertThat(viewModel.isReviewsLoading.value).isFalse()
        assertThat(viewModel.reviewsError.value).isNull()
        assertThat(viewModel.reviews.value).isNotNull()
    }

    @Test
    fun `loadReviews should update reviewsError when fails`() = runTest {

        val errorMessage = "Failed to load reviews"
        val exception = RuntimeException(errorMessage)
        whenever(mockGetReviewsUseCase(movieId)).thenThrow(exception)


        viewModel.loadReviews(movieId)
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        assertThat(viewModel.isReviewsLoading.value).isFalse()
        assertThat(viewModel.reviewsError.value).contains(errorMessage)
        assertThat(viewModel.reviews.value).isNull()
    }

    @Test
    fun `retryReviews should clear error`() = runTest {

        val errorMessage = "Failed to load reviews"
        val exception = RuntimeException(errorMessage)
        whenever(mockGetReviewsUseCase(movieId)).thenThrow(exception)

        viewModel.loadReviews(movieId)
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.reviewsError.value).isNotNull()


        viewModel.retryReviews()
        coroutineTestRule.testDispatcher.scheduler.advanceUntilIdle()


        assertThat(viewModel.reviewsError.value).isNull()
    }
}