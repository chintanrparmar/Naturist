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
import androidx.navigation.fragment.findNavController
import app.chintan.naturist.R
import app.chintan.naturist.databinding.FragmentPostListBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.ui.adapter.BlogListAdapter
import app.chintan.naturist.util.State
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostListFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentPostListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOnClick()
        setUpObserver()
        loadPosts()
    }

    private fun setUpObserver() {
        postViewModel.postList.observe(viewLifecycleOwner, Observer {
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

        val adapter = BlogListAdapter(data) {
            val clickedPost = it as Post
            postViewModel.setSelectedPost(clickedPost)
            findNavController().navigate(R.id.action_navigation_post_list_to_postDetailFragment)
        }

        binding.postRcv.adapter = adapter

    }

    private fun loadPosts() {
        postViewModel.getPosts()
    }

    private fun setUpOnClick() {
        binding.createPostFab.setOnClickListener {
            postViewModel.addPost(Post("https://i.ibb.co/4NWLMf6/mountain.jpg",
                "Mountain Nature",
                "A mountain is an elevated portion of the Earth's crust, generally with steep sides that show significant exposed bedrock. A mountain differs from a plateau in having a limited summit area, and is larger than a hill, typically rising at least 300 metres (1000 feet) above the surrounding land."))
        }
    }


}