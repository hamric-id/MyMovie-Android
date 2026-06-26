package com.hamric.feature.movies.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.hamric.feature.movies.domain.usecase.GetMoviesByGenreUseCase
import com.hamric.feature.movies.utils.CoroutineTestRule
import com.hamric.feature.movies.utils.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: MoviesViewModel
    private val mockUseCase: GetMoviesByGenreUseCase = mock()
    private val genreId = 28

    @Before
    fun setup() {
        viewModel = MoviesViewModel(mockUseCase)
    }

    @Test
    fun `initial state should have null movies, false loading, null error`() = runTest {
        assertThat(viewModel.movies.value).isNull()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `loadMovies should update state to success with movies when use case succeeds`() = runTest {
        val expectedMovies = TestDataFactory.createMovieList(3)
        val pagingData = PagingData.from(expectedMovies)
        whenever(mockUseCase.invoke(genreId)).thenReturn(flowOf(pagingData))

        viewModel.loadMovies(genreId)

        val finalState = viewModel.movies.first { it != null }
        assertThat(finalState).isNotNull()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `loadMovies should update state to Error when use case throws exception`() = runTest {
        val errorMessage = "Failed to load movies"
        val exception = RuntimeException(errorMessage)
        whenever(mockUseCase.invoke(genreId)).thenThrow(exception)

        viewModel.loadMovies(genreId)

        val errorState = viewModel.error.first { it != null }
        assertThat(errorState).contains(errorMessage)
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.movies.value).isNull()
    }

    @Test
    fun `loadMovies should not reload same genre`() = runTest {
        val expectedMovies = TestDataFactory.createMovieList(2)
        val pagingData = PagingData.from(expectedMovies)
        whenever(mockUseCase.invoke(genreId)).thenReturn(flowOf(pagingData))

        viewModel.loadMovies(genreId)
        viewModel.movies.first { it != null }

        viewModel.loadMovies(genreId)

        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.movies.value).isNotNull()
    }

    @Test
    fun `retry should call loadMovies with last genreId`() = runTest {
        val expectedMovies = TestDataFactory.createMovieList(2)
        val pagingData = PagingData.from(expectedMovies)
        whenever(mockUseCase.invoke(genreId)).thenReturn(flowOf(pagingData))

        viewModel.loadMovies(genreId)
        viewModel.movies.first { it != null }

        viewModel.retry()

        viewModel.movies.first { it != null }
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.error.value).isNull()
        assertThat(viewModel.movies.value).isNotNull()
    }

    @Test
    fun `clearError should set error to null`() = runTest {
        val errorMessage = "Test error"
        val exception = RuntimeException(errorMessage)
        whenever(mockUseCase.invoke(genreId)).thenThrow(exception)

        viewModel.loadMovies(genreId)
        viewModel.error.first { it != null }

        assertThat(viewModel.error.value).isNotNull()

        viewModel.clearError()

        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `loadMovies should handle multiple genre changes`() = runTest {
        val genre1 = 28
        val genre2 = 35
        val movies1 = TestDataFactory.createMovieList(2)
        val movies2 = TestDataFactory.createMovieList(3)
        val pagingData1 = PagingData.from(movies1)
        val pagingData2 = PagingData.from(movies2)

        whenever(mockUseCase.invoke(genre1)).thenReturn(flowOf(pagingData1))
        whenever(mockUseCase.invoke(genre2)).thenReturn(flowOf(pagingData2))

        viewModel.loadMovies(genre1)
        viewModel.movies.first { it != null }
        assertThat(viewModel.movies.value).isNotNull()

        viewModel.loadMovies(genre2)
        viewModel.movies.first { it != null }

        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.movies.value).isNotNull()
    }
}