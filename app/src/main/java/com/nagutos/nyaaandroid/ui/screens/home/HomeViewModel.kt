package com.nagutos.nyaaandroid.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.data.local.entity.NyaaDatabase
import com.nagutos.nyaaandroid.data.local.entity.SavedSearch
import com.nagutos.nyaaandroid.data.repository.FavoriteRepository
import com.nagutos.nyaaandroid.data.repository.TorrentRepository
import com.nagutos.nyaaandroid.model.TorrentUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val torrents: List<TorrentUI>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    application: Application,
    private val torrentRepository: TorrentRepository = TorrentRepository(),
) : AndroidViewModel(application) {

    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchCategory by mutableStateOf("0_0")
        private set

    var currentPage by mutableStateOf(1)
        private set

    var searchUser by mutableStateOf<String?>(null)
        private set

    var searchSort by mutableStateOf("id")
        private set

    var searchOrder by mutableStateOf("desc")
        private set

    // Nyaa's "f" query param: 0 = no filter, 1 = no remakes, 2 = trusted only.
    var searchFilter by mutableStateOf(0)
        private set

    private val database = NyaaDatabase.getDatabase(application)
    private val repository = FavoriteRepository(
        database.favoriteDao(),
        database.savedSearchDao()
    )

    val favoriteTorrents: StateFlow<List<FavoriteTorrent>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedSearches = repository.allSavedSearches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadTorrents()
    }

    fun onSearch(
        query: String,
        category: String,
        sort: String = "id",
        order: String = "desc",
        filter: Int = 0
    ) {
        this.searchUser = null
        this.searchQuery = query
        this.searchCategory = category
        this.searchSort = sort
        this.searchOrder = order
        this.searchFilter = filter
        this.currentPage = 1
        loadTorrents()
    }

    fun saveCurrentSearch(
        label: String,
        query: String,
        category: String,
        sort: String,
        order: String
    ) {
        viewModelScope.launch {
            repository.insertSavedSearch(
                SavedSearch(label = label, query = query, category = category)
            )
        }
    }

    fun deleteSavedSearch(search: SavedSearch) {
        viewModelScope.launch {
            repository.deleteSavedSearch(search)
        }
    }

    fun onUserSearch(username: String) {
        searchUser = username
        searchQuery = ""
        currentPage = 1
        loadTorrents()
    }

    fun nextPage() {
        currentPage++
        loadTorrents()
    }

    fun previousPage() {
        if (currentPage > 1) {
            currentPage--
            loadTorrents()
        }
    }

    fun toggleFavorite(torrent: TorrentUI, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                repository.deleteById(torrent.id)
            } else {
                repository.addFavorite(torrent)
            }
        }
    }

    fun isFavorite(torrentId: String): Flow<Boolean> = repository.isFavorite(torrentId)

    fun loadTorrents() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            try {
                val items = torrentRepository.getTorrents(
                    query = searchQuery,
                    category = searchCategory,
                    page = currentPage,
                    user = searchUser,
                    sort = searchSort,
                    order = searchOrder,
                    filter = searchFilter
                )
                // Keep the (empty) success state on out-of-range pages so the UI can offer
                // to step back instead of showing an error.
                uiState = HomeUiState.Success(items)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load torrents", e)
                uiState = HomeUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
