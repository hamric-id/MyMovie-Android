package com.hamric.feature.details.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hamric.core.model.Review
import com.hamric.feature.details.domain.usecase.GetMovieDetailsUseCase
import com.hamric.feature.details.domain.usecase.GetMovieReviewsUseCase
import com.hamric.feature.details.domain.usecase.GetMovieTrailerUseCase
import com.hamric.feature.details.presentation.state.MovieDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val getMovieReviewsUseCase: GetMovieReviewsUseCase,
    private val getMovieTrailerUseCase: GetMovieTrailerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _reviews = MutableStateFlow<Flow<PagingData<Review>>?>(null)
    val reviews: StateFlow<Flow<PagingData<Review>>?> = _reviews.asStateFlow()

    private val _isReviewsLoading = MutableStateFlow(false)
    val isReviewsLoading: StateFlow<Boolean> = _isReviewsLoading.asStateFlow()

    private val _reviewsError = MutableStateFlow<String?>(null)
    val reviewsError: StateFlow<String?> = _reviewsError.asStateFlow()

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading

            val detailsDeferred = async { getMovieDetailsUseCase(movieId) }
            val trailerDeferred = async { getMovieTrailerUseCase(movieId) }

            val detailsResult = detailsDeferred.await()
            val trailerResult = trailerDeferred.await()

            detailsResult.fold(
                onSuccess = { movie ->
                    val trailer = trailerResult.getOrNull()
                    _uiState.value = MovieDetailUiState.Success(movie, trailer)
                },
                onFailure = { exception ->
                    _uiState.value = MovieDetailUiState.Error(
                        exception.message ?: "Failed to load movie details"
                    )
                }
            )
        }
    }

    fun loadReviews(movieId: Int) {
        viewModelScope.launch {
            _isReviewsLoading.value = true
            _reviewsError.value = null

            try {
                val pagingFlow = getMovieReviewsUseCase(movieId)
                    .cachedIn(viewModelScope)
                _reviews.value = pagingFlow
                _isReviewsLoading.value = false
            } catch (e: Exception) {
                _reviewsError.value = e.message ?: "Failed to load reviews"
                _isReviewsLoading.value = false
            }
        }
    }

    fun retryReviews() {
        _reviewsError.value = null
    }
}