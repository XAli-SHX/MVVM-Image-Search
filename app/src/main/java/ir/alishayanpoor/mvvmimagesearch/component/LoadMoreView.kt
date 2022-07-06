package ir.alishayanpoor.mvvmimagesearch.component

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import ir.alishayanpoor.mvvmimagesearch.R
import ir.alishayanpoor.mvvmimagesearch.databinding.ViewLoadMoreBinding

class LoadMoreView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private lateinit var binding: ViewLoadMoreBinding

    init {
        val view = inflate(context, R.layout.view_load_more, this)
        binding = ViewLoadMoreBinding.bind(view)
    }

    fun startLoading() {
        binding.apply {
            btnRetry.visibility = GONE
            tvError.visibility = GONE
            progressBar.visibility = VISIBLE
        }
        show()
    }

    fun cancelLoading() {
        binding.apply {
            btnRetry.visibility = GONE
            tvError.visibility = GONE
            progressBar.visibility = VISIBLE
        }
        hide()
    }

    fun show() {
        binding.root.visibility = VISIBLE
    }

    fun hide() {
        binding.root.visibility = GONE
    }


    fun setError(error: String, onRetryButtonClicked: () -> Unit) {
        show()
        binding.apply {
            btnRetry.visibility = VISIBLE
            tvError.visibility = VISIBLE
            progressBar.visibility = GONE
            btnRetry.setOnClickListener { onRetryButtonClicked.invoke() }
        }
    }
}