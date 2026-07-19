package com.nagutos.nyaaandroid.ui.screens.detail

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nagutos.nyaaandroid.data.local.entity.FavoriteTorrent
import com.nagutos.nyaaandroid.data.local.entity.NyaaDatabase
import com.nagutos.nyaaandroid.data.repository.FavoriteRepository
import com.nagutos.nyaaandroid.data.repository.TorrentRepository
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.model.TorrentUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val detail: TorrentDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

/**
 * ViewModel scoped to a single torrent detail screen. Fetches the detail through
 * [TorrentRepository] and exposes the favorites needed to render (and toggle) the heart.
 */
class DetailViewModel(
    application: Application,
    private val torrentRepository: TorrentRepository = TorrentRepository(),
) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteRepository(
        NyaaDatabase.getDatabase(application).favoriteDao(),
        NyaaDatabase.getDatabase(application).savedSearchDao()
    )

    var uiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    val favoriteTorrents: StateFlow<List<FavoriteTorrent>> = favoriteRepository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadDetail(url: String) {
        viewModelScope.launch {
            uiState = DetailUiState.Loading
            try {
                uiState = DetailUiState.Success(torrentRepository.getDetail(url))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load torrent detail for $url", e)
                uiState = DetailUiState.Error(e.message ?: "Erreur de connexion")
            }
        }
    }

    /**
     * Download the .torrent at [sourceUrl] and write it to the user-picked [destUri]
     * (from the Storage Access Framework, so it may be local or a remote/cloud provider).
     * [onResult] is invoked on the main thread with success/failure for a toast.
     */
    fun saveTorrentToUri(sourceUrl: String, destUri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = try {
                val bytes = torrentRepository.downloadBytes(sourceUrl)
                withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openOutputStream(destUri)?.use { it.write(bytes) }
                        ?: throw IOException("Could not open output stream for $destUri")
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save .torrent from $sourceUrl", e)
                false
            }
            onResult(success)
        }
    }

    fun toggleFavorite(torrent: TorrentUI, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                favoriteRepository.deleteById(torrent.id)
            } else {
                favoriteRepository.addFavorite(torrent)
            }
        }
    }

    private companion object {
        const val TAG = "DetailViewModel"
    }
}

class DetailViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
