package ir.alishayanpoor.mvvmimagesearch.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.alishayanpoor.mvvmimagesearch.R
import ir.alishayanpoor.mvvmimagesearch.databinding.FragmentGalleryBinding
import ir.alishayanpoor.mvvmimagesearch.util.Constants
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery) {
    private val viewModel by viewModels<GalleryViewModel>()
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGalleryBinding.bind(view)
        val adapter = UnsplashPhotoAdapter { photo ->
            val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(photo)
            findNavController().navigate(action)
        }
        initViews(adapter)
        subscribeToObservables(adapter)
        if (viewModel.photosLiveData.value.isNullOrEmpty()) {
            viewModel.startLoading()
        }
        setHasOptionsMenu(true)
    }

    private fun initViews(adapter: UnsplashPhotoAdapter) {
        binding.apply {
            loadMoreView.hide()
            rvPictures.apply {
                setHasFixedSize(true)
                this.adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val layoutManager = rvPictures.layoutManager as LinearLayoutManager
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        Constants.log("lastVisibleItem = $lastVisibleItem")
                        Constants.log("childCount = ${adapter.itemCount}")
                        if (lastVisibleItem >= adapter.itemCount - 3) {
                            Constants.log("lastVisibleItem == adapter.itemCount")
                            Constants.log("viewModel.isLoading = ${viewModel.isLoading}")
                            if (!viewModel.isLoading) {
                                loadMorePhotos(false)
                                /*Snackbar.make(
                                    requireView(),
                                    "Loading page ${viewModel.lastLoadedPage + 1} loaded",
                                    Snackbar.LENGTH_SHORT
                                ).show()*/
                            }
                        }
                        val lastCompleteVisible =
                            layoutManager.findLastCompletelyVisibleItemPosition()
                        Constants.log("lastCompleteVisible = $lastCompleteVisible")
                        if (lastCompleteVisible == adapter.itemCount - 1) {
                            binding.loadMoreView.startLoading()
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeToObservables(adapter: UnsplashPhotoAdapter) {
        viewModel.photosLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.galleryEvent.collect {
                when (it) {
                    is GalleryViewModel.PhotosLiveDataResponse.Success -> {
                        binding.loadMoreView.cancelLoading()
                    }
                    is GalleryViewModel.PhotosLiveDataResponse.Error -> {
                        binding.loadMoreView.setError("Error happened while loading the page") {
                            loadMorePhotos(true)
                        }
                        Constants.log(it.responseBody.toString())
                    }
                    is GalleryViewModel.PhotosLiveDataResponse.Failed -> {
                        binding.loadMoreView.setError("Failed to load page") {
                            loadMorePhotos(true)
                        }
                        Constants.log(it.message)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallery, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    binding.rvPictures.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun loadMorePhotos(loadAnimation: Boolean) {
        if (loadAnimation) binding.loadMoreView.startLoading()
        viewModel.loadMore()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}