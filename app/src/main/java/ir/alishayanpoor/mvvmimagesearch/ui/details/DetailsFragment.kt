package ir.alishayanpoor.mvvmimagesearch.ui.details

import android.Manifest
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import ir.alishayanpoor.mvvmimagesearch.R
import ir.alishayanpoor.mvvmimagesearch.data.UnsplashPhoto
import ir.alishayanpoor.mvvmimagesearch.databinding.FragmentDetailsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val viewModel by viewModels<DetailsViewModel>()
    private val args by navArgs<DetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDetailsBinding.bind(view)
        binding.apply {
            val photo = args.photo
            Glide.with(this@DetailsFragment)
                .load(photo.urls.full)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        tvCreator.isVisible = true
                        tvDescription.isVisible = !photo.description.isNullOrBlank()
                        fabDownload.isVisible = true
                        return false
                    }
                })
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(image)

            tvDescription.text = photo.description

            fabDownload.setOnClickListener {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Dexter.withContext(context)
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                save2(binding.image)
//                                save(photo)
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                token?.continuePermissionRequest()
                            }
                        }).check()
                } else {
//                    save(photo)
                    save2(binding.image)
                }
            }

            val uri = Uri.parse(photo.user.attributionUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            tvCreator.apply {
                text = "Photo by ${photo.user.name} on Unsplash"
                setOnClickListener {
                    context.startActivity(intent)
                }
                paint.isUnderlineText = true
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.detailsEvent.collect { event ->
                var message = ""
                when (event) {
                    is DetailsViewModel.DetailsEvent.SavedImageIntoStorage -> {
                        message = "Image saved successfully"
                    }
                    is DetailsViewModel.DetailsEvent.IOException -> {
                        message = "IOException: ${event.error}"
                    }
                    is DetailsViewModel.DetailsEvent.Exception -> {
                        message = "Exception: ${event.error}"
                    }
                    is DetailsViewModel.DetailsEvent.Error -> {
                        message = "Error: ${event.errorBody.string()}"
                    }
                    is DetailsViewModel.DetailsEvent.NullBitmap -> {
                        message = "Null bitmap"
                    }
                    is DetailsViewModel.DetailsEvent.Downloaded -> {
                        message = "Photo downloaded"
                    }
                    is DetailsViewModel.DetailsEvent.HttpException -> {
                        message = "HttpException: ${event.error}"
                    }
                }
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun save2(image: AppCompatImageView) = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.saveImage2(
            requireContext().applicationContext.contentResolver,
            (image.drawable as BitmapDrawable).bitmap
        )
    }

    private fun save(photo: UnsplashPhoto) {
        viewModel.saveImage(
            requireContext().applicationContext.contentResolver,
            photo
        )
    }
}