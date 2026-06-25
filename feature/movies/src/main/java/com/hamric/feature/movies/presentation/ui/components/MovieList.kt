package com.hamric.feature.movies.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.hamric.core.model.Movie
import kotlinx.coroutines.flow.Flow

@Composable
fun MovieList(
    movies: Flow<PagingData<Movie>>?,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagingItems: LazyPagingItems<Movie>? = movies?.collectAsLazyPagingItems()

    if (pagingItems == null) {
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index -> pagingItems[index]?.id ?: index }
        ) { index ->
            val movie = pagingItems[index]
            movie?.let {
                MovieItem(
                    movie = it,
                    onClick = onMovieClick
                )
            }
        }

        if (pagingItems.loadState.append is LoadState.Loading) {
            item {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        if (pagingItems.loadState.append is LoadState.Error) {
            item {
                ErrorState(
                    message = "Failed to load more movies",
                    onRetry = { pagingItems.retry() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}