package ir.alishayanpoor.mvvmimagesearch.api

import com.google.gson.annotations.SerializedName
import ir.alishayanpoor.mvvmimagesearch.data.UnsplashPhoto

data class UnsplashResponse(
    @SerializedName("results")
    val results: List<UnsplashPhoto>
)