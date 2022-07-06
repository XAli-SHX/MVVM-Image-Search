package ir.alishayanpoor.mvvmimagesearch.ui.details

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.alishayanpoor.mvvmimagesearch.api.UnsplashApi
import ir.alishayanpoor.mvvmimagesearch.data.UnsplashPhoto
import ir.alishayanpoor.mvvmimagesearch.util.Constants
import ir.alishayanpoor.mvvmimagesearch.util.sdk29AndUp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.util.*
import javax.inject.Inject


@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val unsplashApi: UnsplashApi,
) : ViewModel() {

    private val detailsEventChannel = Channel<DetailsEvent>()
    val detailsEvent = detailsEventChannel.receiveAsFlow()

    fun saveImage(contentResolver: ContentResolver, photo: UnsplashPhoto) = viewModelScope.launch {
        Constants.log("here")
        try {
            val bitmap: Bitmap? = getBitmapFromUrl(photo)
//            val bitmap: Bitmap? = getBitmapFromUrl(photo)
            if (bitmap != null) {
                val name = photo.id + photo.user.username + UUID.randomUUID()
                val savedSuccessFully = savePhoto(
                    contentResolver,
                    name,
                    bitmap
                )
                if (!savedSuccessFully) {
                    detailsEventChannel.send(DetailsEvent.IOException(""))
                } else {
                    detailsEventChannel.send(DetailsEvent.SavedImageIntoStorage(name))
                }
            } else {
                detailsEventChannel.send(DetailsEvent.NullBitmap)
            }
        } catch (e: IOException) {
            detailsEventChannel.send(DetailsEvent.IOException(e.localizedMessage))
        } catch (e: HttpException) {
            detailsEventChannel.send(DetailsEvent.HttpException(e.localizedMessage))
        } catch (e: Exception) {
            detailsEventChannel.send(DetailsEvent.Exception(e.localizedMessage))
        }
    }

    fun saveImage2(contentResolver: ContentResolver, bitmap: Bitmap) = viewModelScope.launch {
        Constants.log("here2")
        try {
            val name = UUID.randomUUID().toString()
            val savedSuccessFully = savePhoto(
                contentResolver,
                name,
                bitmap
            )
            if (!savedSuccessFully) {
                detailsEventChannel.send(DetailsEvent.IOException(""))
            } else {
                detailsEventChannel.send(DetailsEvent.SavedImageIntoStorage(name))
            }
        } catch (e: IOException) {
            detailsEventChannel.send(DetailsEvent.IOException(e.localizedMessage))
        } catch (e: HttpException) {
            detailsEventChannel.send(DetailsEvent.HttpException(e.localizedMessage))
        } catch (e: Exception) {
            detailsEventChannel.send(DetailsEvent.Exception(e.localizedMessage))
        }
    }

    private fun savePhoto(
        contentResolver: ContentResolver,
        name: String,
        bitmap: Bitmap
    ): Boolean {
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 99, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create Mediastore entry")
            true
        } catch (e: IOException) {
            false
        }
    }

    @Throws(HttpException::class, Exception::class)
    private suspend fun getBitmapFromUrl(photo: UnsplashPhoto): Bitmap? {
        Constants.log("image url: ${photo.urls.full}")
        /*val call = unsplashApi.downloadPhoto(photo.urls.full)
        val bytes = call
//        val bytes = call.body()
        detailsEventChannel.send(DetailsEvent.Downloaded(bytes))
        return if (bytes != null) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }*/
        return null
    }


    sealed class DetailsEvent {
        data class SavedImageIntoStorage(val name: String) : DetailsEvent()
        data class Error(val errorBody: ResponseBody) : DetailsEvent()
        data class IOException(val error: String?) : DetailsEvent()
        data class HttpException(val error: String?) : DetailsEvent()
        data class Exception(val error: String?) : DetailsEvent()
        object NullBitmap : DetailsEvent()
        data class Downloaded(val byte: ByteArray?) : DetailsEvent()
    }
}