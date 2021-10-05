package app.chintan.naturist.ui.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import app.chintan.naturist.databinding.FragmentPostDetailBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.util.State
import coil.load
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentPostDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnClick()
        loadPostDetail()
    }

    private fun loadPostDetail() {
        postViewModel.selectedPostLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is State.Success -> {
                    updatePostDetailUI(it.data)
                    binding.progressBar.visibility = GONE
                }
                is State.Error -> {
                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = GONE
                }
                is State.Loading -> binding.progressBar.visibility = VISIBLE
            }

        })

    }


    private fun updatePostDetailUI(data: Post) {
        binding.postImage.load(data.imageUrl)
        binding.postTitle.text = data.title
        binding.postDesc.text = data.description
    }

    private fun setUpOnClick() {
    }


}