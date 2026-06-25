package com.hamric.feature.genres.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.feature.genres.domain.usecase.GetGenresUseCase
import com.hamric.feature.genres.presentation.state.GenresUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenresUiState>(GenresUiState.Loading)
    val uiState: StateFlow<GenresUiState> = _uiState.asStateFlow()

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = GenresUiState.Loading
            val result = getGenresUseCase()
            result.fold(
                onSuccess = { genres ->
                    _uiState.value = if (genres.isEmpty()) {
                        GenresUiState.Error("No genres available")
                    } else {
                        GenresUiState.Success(genres)
                    }
                },
                onFailure = { exception ->
                    _uiState.value = GenresUiState.Error(
                        exception.message ?: "Failed to load genres"
                    )
                }
            )
        }
    }
}