package ir.alishayanpoor.mvvmimagesearch.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ir.alishayanpoor.mvvmimagesearch.R
import ir.alishayanpoor.mvvmimagesearch.data.UnsplashPhoto
import ir.alishayanpoor.mvvmimagesearch.databinding.ItemUnsplashPhotoBinding

class UnsplashPhotoAdapter(private val listener: (photo: UnsplashPhoto) -> Unit) :
    ListAdapter<UnsplashPhoto, UnsplashPhotoAdapter.UnsplashViewHolder>(DiffCallback()) {

    override fun onBindViewHolder(holder: UnsplashViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnsplashViewHolder {
        val binding =
            ItemUnsplashPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnsplashViewHolder(binding)
    }

    inner class UnsplashViewHolder(private val binding: ItemUnsplashPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    item?.let {
                        listener.invoke(it)
                    }
                }
            }
        }

        fun bind(photo: UnsplashPhoto) {
            binding.apply {
                Glide.with(itemView).load(photo.urls.regular)
                    .centerCrop().transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_baseline_error_outline_24)
                    .into(image)
                tvUsername.text = photo.user.username
                tvLikes.text = photo.likes.toString()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UnsplashPhoto>() {
        override fun areItemsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: UnsplashPhoto,
            newItem: UnsplashPhoto
        ): Boolean = oldItem == newItem
    }
}