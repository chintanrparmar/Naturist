package app.chintan.naturist.ui.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import app.chintan.naturist.R
import app.chintan.naturist.databinding.FragmentFavouriteBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.ui.adapter.PostListAdapter
import app.chintan.naturist.ui.blog.PostViewModel
import app.chintan.naturist.util.State
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentFavouriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObserver()
        loadPosts()
    }

    private fun setUpObserver() {
        postViewModel.favouritePostList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is State.Success -> {
                    updateBlogListUI(it.data)
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

    private fun updateBlogListUI(data: List<Post>) {
        val adapter = PostListAdapter(data) {
            val clickedPost = it as Post
            postViewModel.setSelectedPost(clickedPost)
            findNavController().navigate(R.id.action_navigation_favourite_to_postDetailFragment)
        }

        binding.postRcv.adapter = adapter

    }

    private fun loadPosts() {
        postViewModel.getFavouritePosts()
    }

}