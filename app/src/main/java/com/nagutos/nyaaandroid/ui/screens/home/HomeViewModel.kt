package com.nagutos.nyaaandroid.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.data.local.entity.NyaaDatabase
import com.nagutos.nyaaandroid.data.local.entity.SavedSearch
import com.nagutos.nyaaandroid.data.repository.FavoriteRepository
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.network.NyaaHtmlParser
import com.nagutos.nyaaandroid.network.NyaaNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val torrents: List<TorrentUI>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val detail: TorrentDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class HomeViewModel(application: Application) : AndroidViewModel(application){

    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchCategory by mutableStateOf("0_0")
        private set

    var currentPage by mutableStateOf(1)
        private set

    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    var searchUser by mutableStateOf<String?>(null)
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

    fun onSearch(query: String, category: String) {
        this.searchUser = null
        this.searchQuery = query
        this.searchCategory = category
        this.currentPage = 1
        loadTorrents()
    }

    fun saveCurrentSearch(label: String, query: String, category: String) {
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

    // Pour vérifier si un torrent spécifique est favori (utile pour l'écran détail)
    fun isFavorite(torrentId: String): Flow<Boolean> = repository.isFavorite(torrentId)


    fun loadTorrents() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            try {
                val items = withContext(Dispatchers.IO) {
                    val responseBody = NyaaNetwork.api.getTorrentsHtml(
                        query = searchQuery,
                        category = searchCategory,
                        page = currentPage,
                        user = searchUser
                    )
                    val htmlString = responseBody.string()
                    NyaaHtmlParser.parseTorrents(htmlString)
                }

                if (items.isEmpty() && currentPage > 1) {
                    uiState = HomeUiState.Success(emptyList())
                } else {
                    uiState = HomeUiState.Success(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = HomeUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun loadDetail(url: String) {
        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            try {
                // On bascule sur le thread IO pour le réseau et le parsing Jsoup
                val detail = withContext(Dispatchers.IO) {
                    val response = NyaaNetwork.api.getTorrentDetailHtml(url)
                    val html = response.string()

                    // Parsing Jsoup de la page entière
                    NyaaHtmlParser.parseDetail(html)
                }
                detailUiState = DetailUiState.Success(detail)
            } catch (e: Exception) {
                e.printStackTrace()
                detailUiState = DetailUiState.Error(e.message ?: "Erreur de connexion")
            }
        }
    }
}
