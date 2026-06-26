package com.hamric.feature.movies.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hamric.core.model.Movie
import com.hamric.feature.movies.domain.usecase.GetMoviesByGenreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMoviesByGenreUseCase: GetMoviesByGenreUseCase
) : ViewModel() {

    private val _genreId = MutableStateFlow<Int?>(null)
    private val _movies = MutableStateFlow<Flow<PagingData<Movie>>?>(null)
    val movies: StateFlow<Flow<PagingData<Movie>>?> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadMovies(genreId: Int) {
        if (_genreId.value == genreId) return

        viewModelScope.launch {
            _genreId.value = genreId
            _isLoading.value = true
            _error.value = null

            try {
                val pagingFlow = getMoviesByGenreUseCase(genreId)
                    .cachedIn(viewModelScope)
                _movies.value = pagingFlow
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load movies"
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        _genreId.value?.let { loadMovies(it) }
    }

    fun clearError() {
        _error.value = null
    }
}