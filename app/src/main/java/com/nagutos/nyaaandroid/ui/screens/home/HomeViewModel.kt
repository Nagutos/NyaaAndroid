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
                val detail = withContext(Dispatchers.IO) {
                    // The parser often returns “/view/123456”; you need to add the domain.
                    val fullUrl = if(url.startsWith("http")) url else "https://nyaa.si$url"
                    val responseBody = NyaaNetwork.api.getTorrentDetailHtml(fullUrl)
                    val htmlString = responseBody.string()

                    NyaaHtmlParser.parseDetail(htmlString)
                }
                detailUiState = DetailUiState.Success(detail)
            } catch (e: Exception) {
                e.printStackTrace()
                detailUiState = DetailUiState.Error("Impossible de charger le détail : ${e.message}")
            }
        }
    }
}