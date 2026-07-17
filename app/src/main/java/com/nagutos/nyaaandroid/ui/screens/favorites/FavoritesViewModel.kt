package com.nagutos.nyaaandroid.ui.screens.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.data.local.entity.NyaaDatabase
import com.nagutos.nyaaandroid.data.repository.FavoriteRepository
import com.nagutos.nyaaandroid.model.TorrentUI
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favorites-only view model with no network work.
 *
 * Used by the bottom-bar badge (MainActivity) and by FavoritesScreen so neither triggers the
 * torrent fetch that HomeViewModel.init runs — those screens only ever need the favorites list.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoriteRepository(
        NyaaDatabase.getDatabase(application).favoriteDao(),
        NyaaDatabase.getDatabase(application).savedSearchDao()
    )

    val favoriteTorrents: StateFlow<List<FavoriteTorrent>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(torrent: TorrentUI, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                repository.deleteById(torrent.id)
            } else {
                repository.addFavorite(torrent)
            }
        }
    }
}

class FavoritesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
