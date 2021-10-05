package app.chintan.naturist.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import app.chintan.naturist.databinding.FragmentHomeBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.ui.adapter.BlogListAdapter
import app.chintan.naturist.util.State
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        homeViewModel.postList.observe(viewLifecycleOwner, Observer {
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

        }

        binding.postRcv.adapter = adapter

    }

    private fun loadPosts() {
        homeViewModel.getPosts()
    }

    private fun setUpOnClick() {
        binding.createPostFab.setOnClickListener {
            homeViewModel.addPost(Post("https://i.ibb.co/4NWLMf6/mountain.jpg",
                "Mountain Nature",
                "A mountain is an elevated portion of the Earth's crust, generally with steep sides that show significant exposed bedrock. A mountain differs from a plateau in having a limited summit area, and is larger than a hill, typically rising at least 300 metres (1000 feet) above the surrounding land."))
        }
    }


}