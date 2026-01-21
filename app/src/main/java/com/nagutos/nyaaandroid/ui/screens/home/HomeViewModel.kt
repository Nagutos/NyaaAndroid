package com.nagutos.nyaaandroid.ui.screens.home

import com.nagutos.nyaaandroid.network.NyaaNetwork
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.network.NyaaHtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class HomeViewModel : ViewModel() {

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

    init {
        loadTorrents()
    }

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