package com.nagutos.nyaaandroid.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.network.NyaaApiService.NyaaNetwork
import com.nagutos.nyaaandroid.network.NyaaHtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// État pour la liste principale
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val torrents: List<TorrentUI>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

// État pour le détail (NOUVEAU)
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val detail: TorrentDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class HomeViewModel : ViewModel() {

    // --- Gestion de la Liste ---
    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchCategory by mutableStateOf("0_0")
        private set

    var currentPage by mutableStateOf(1)
        private set

    // --- Gestion du Détail (NOUVEAU) ---
    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    init {
        loadTorrents()
    }

    // --- Fonctions Liste ---
    fun onSearch(query: String, category: String) {
        searchQuery = query
        searchCategory = category
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

    fun loadTorrents() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            try {
                val items = withContext(Dispatchers.IO) {
                    val responseBody = NyaaNetwork.api.getTorrentsHtml(
                        query = searchQuery,
                        category = searchCategory,
                        page = currentPage
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

    // --- Fonction Détail (Celle qui posait problème) ---
    fun loadDetail(url: String) {
        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            try {
                val detail = withContext(Dispatchers.IO) {
                    // Jsoup connect direct pour récupérer la page détail
                    // On ajoute "https://nyaa.si" devant car l'url est souvent "/view/..."
                    val fullUrl = if(url.startsWith("http")) url else "https://nyaa.si$url"
                    val doc = org.jsoup.Jsoup.connect(fullUrl).get()
                    NyaaHtmlParser.parseDetail(doc.outerHtml())
                }
                detailUiState = DetailUiState.Success(detail)
            } catch (e: Exception) {
                e.printStackTrace()
                detailUiState = DetailUiState.Error("Impossible de charger le détail : ${e.message}")
            }
        }
    }
}