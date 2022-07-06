package ir.alishayanpoor.mvvmimagesearch.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnsplashPhoto(
    @SerializedName("id")
    val id: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("urls")
    val urls: UnsplashPhotoUrls,
    @SerializedName("user")
    val user: UnsplashUser,
    @SerializedName("likes")
    val likes: Int
) : Parcelable {
    @Parcelize
    data class UnsplashPhotoUrls(
        @SerializedName("raw")
        val raw: String,
        @SerializedName("full")
        val full: String,
        @SerializedName("regular")
        val regular: String,
        @SerializedName("small")
        val small: String,
        @SerializedName("thumb")
        val thumb: String,
    ) : Parcelable

    @Parcelize
    data class UnsplashUser(
        @SerializedName("name")
        val name: String,
        @SerializedName("username")
        val username: String,
    ) : Parcelable {
        val attributionUrl get() = "https://unsplash.com/$username?utm_source=ImageSearchApp&utm_medium=referral"
    }
}