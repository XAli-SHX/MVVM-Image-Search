package ir.alishayanpoor.mvvmimagesearch.ui.gallery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.alishayanpoor.mvvmimagesearch.api.UnsplashApi
import ir.alishayanpoor.mvvmimagesearch.data.UnsplashPhoto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject

const val PER_PAGE = 10

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val unsplashApi: UnsplashApi,
    state: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val DEFAULT_QUERY = "nature"
        private const val SAVED_STATE_QUERY = "current_query"
    }

    var isLoading: Boolean = false
    var lastLoadedPage: Int = 1
    val photosLiveData = MutableLiveData<ArrayList<UnsplashPhoto>>()
    private var query: String = state.get(SAVED_STATE_QUERY) ?: DEFAULT_QUERY
    private val galleryEventChannel = Channel<PhotosLiveDataResponse>()
    val galleryEvent = galleryEventChannel.receiveAsFlow()


    fun searchPhotos(query: String) {
        if (this.query == query) return
        this.query = query
        startLoading()
    }

    fun loadMore() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = unsplashApi.searchPhotos(query, lastLoadedPage + 1, PER_PAGE)
                if (response.isSuccessful) {
                    lastLoadedPage++
                    val newPhotos = response.body()?.results
                    var photos = photosLiveData.value
                    if (newPhotos != null) {
                        if (photos != null) {
                            photos.addAll(newPhotos)
                            photosLiveData.postValue(photos!!)
                        } else {
                            photos = ArrayList(newPhotos)
                            photosLiveData.postValue(photos!!)
                        }
                        galleryEventChannel.send(
                            PhotosLiveDataResponse.Success(
                                lastLoadedPage,
                                newPhotos
                            )
                        )
                    }
                    galleryEventChannel.send(
                        PhotosLiveDataResponse.Success(
                            lastLoadedPage,
                            newPhotos
                        )
                    )
                } else {
                    galleryEventChannel.send(
                        PhotosLiveDataResponse.Error(lastLoadedPage, response.errorBody()),
                    )
                }
            } catch (e: HttpException) {
                galleryEventChannel.send(
                    PhotosLiveDataResponse.Failed(
                        lastLoadedPage, e.localizedMessage.orEmpty()
                    )
                )
            } catch (e: Exception) {
                galleryEventChannel.send(
                    PhotosLiveDataResponse.Failed(
                        lastLoadedPage, e.localizedMessage.orEmpty()
                    )
                )
            }
            isLoading = false
        }
    }

    fun startLoading() {
        lastLoadedPage = 0
        photosLiveData.value = ArrayList()
        loadMore()
    }

    sealed class PhotosLiveDataResponse {
        data class Success(val page: Int, val newPhotos: List<UnsplashPhoto>?) :
            PhotosLiveDataResponse()

        data class Error(val page: Int, val responseBody: ResponseBody?) : PhotosLiveDataResponse()
        data class Failed(val page: Int, val message: String) : PhotosLiveDataResponse()
    }
}