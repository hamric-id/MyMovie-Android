package com.hamric.feature.genres.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hamric.core.model.Genre
import com.hamric.feature.genres.domain.usecase.GetGenresUseCase
import com.hamric.feature.genres.presentation.state.GenresUiState
import com.hamric.feature.genres.utils.CoroutineTestRule
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
class GenresViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: GenresViewModel
    private val mockUseCase: GetGenresUseCase = mock()

    @Before
    fun setup() {
        viewModel = GenresViewModel(mockUseCase)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val initialState = viewModel.uiState.first()
        assertThat(initialState).isInstanceOf(GenresUiState.Loading::class.java)
    }

    @Test
    fun `loadGenres should update state to Success with genres when use case succeeds`() = runTest {
        val expectedGenres = listOf(
            Genre(id = 28u, name = "Action"),
            Genre(id = 35u, name = "Comedy"),
            Genre(id = 18u, name = "Drama")
        )
        whenever(mockUseCase()).thenReturn(Result.success(expectedGenres))

        viewModel.loadGenres()

        val finalState = viewModel.uiState.first { it is GenresUiState.Success }
        assertThat(finalState).isInstanceOf(GenresUiState.Success::class.java)
        val successState = finalState as GenresUiState.Success
        assertThat(successState.genres).isEqualTo(expectedGenres)
    }

    @Test
    fun `loadGenres should update state to Error when use case fails`() = runTest {
        val errorMessage = "Failed to load genres"
        val exception = Exception(errorMessage)
        whenever(mockUseCase()).thenReturn(Result.failure(exception))

        viewModel.loadGenres()

        val finalState = viewModel.uiState.first { it is GenresUiState.Error }
        assertThat(finalState).isInstanceOf(GenresUiState.Error::class.java)
        val errorState = finalState as GenresUiState.Error
        assertThat(errorState.message).contains(errorMessage)
    }

    @Test
    fun `loadGenres should update state to Error when use case returns empty list`() = runTest {
        whenever(mockUseCase()).thenReturn(Result.success(emptyList()))

        viewModel.loadGenres()

        val finalState = viewModel.uiState.first { it is GenresUiState.Error }
        assertThat(finalState).isInstanceOf(GenresUiState.Error::class.java)
        val errorState = finalState as GenresUiState.Error
        assertThat(errorState.message).isEqualTo("No genres available")
    }

    @Test
    fun `loadGenres should show Loading state before each request`() = runTest {
        val genres = listOf(Genre(id = 28u, name = "Action"))
        whenever(mockUseCase()).thenReturn(Result.success(genres))

        val initialState = viewModel.uiState.first()
        assertThat(initialState).isInstanceOf(GenresUiState.Loading::class.java)

        viewModel.loadGenres()

        val successState = viewModel.uiState.first { it is GenresUiState.Success }
        assertThat(successState).isInstanceOf(GenresUiState.Success::class.java)
        assertThat((successState as GenresUiState.Success).genres).isEqualTo(genres)
    }

    @Test
    fun `loadGenres should handle multiple rapid calls correctly`() = runTest {
        val genres1 = listOf(Genre(id = 28u, name = "Action"))
        val genres2 = listOf(Genre(id = 35u, name = "Comedy"))

        whenever(mockUseCase())
            .thenReturn(Result.success(genres1))
            .thenReturn(Result.success(genres2))

        val initialState = viewModel.uiState.first()
        assertThat(initialState).isInstanceOf(GenresUiState.Loading::class.java)

        viewModel.loadGenres()
        val success1 = viewModel.uiState.first { it is GenresUiState.Success }
        assertThat(success1).isInstanceOf(GenresUiState.Success::class.java)
        assertThat((success1 as GenresUiState.Success).genres).isEqualTo(genres1)

        viewModel.loadGenres()
        val success2 = viewModel.uiState.first { it is GenresUiState.Success }
        assertThat(success2).isInstanceOf(GenresUiState.Success::class.java)
        assertThat((success2 as GenresUiState.Success).genres).isEqualTo(genres2)
    }
}