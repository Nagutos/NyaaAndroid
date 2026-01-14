package com.nagutos.nyaaandroid.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.model.toUiModel
import com.nagutos.nyaaandroid.network.NyaaNetwork
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val torrents: List<TorrentUI>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel : ViewModel() {

    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    init {
        loadTorrents()
    }

    fun loadTorrents() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading // On affiche le chargement
            try {
                val rssFeed = NyaaNetwork.api.getRssFeed()

                val items = rssFeed.channel?.items?.map { it.toUiModel() } ?: emptyList()

                uiState = HomeUiState.Success(items)
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = HomeUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}