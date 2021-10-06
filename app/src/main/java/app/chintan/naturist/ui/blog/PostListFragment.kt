package app.chintan.naturist.ui.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.chintan.naturist.R
import app.chintan.naturist.databinding.FragmentPostListBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.model.UserRole
import app.chintan.naturist.ui.adapter.PostListAdapter
import app.chintan.naturist.util.Constants.ROLE_KEY
import app.chintan.naturist.util.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostListFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentPostListBinding? = null

    @Inject
    lateinit var prefDataStore: DataStore<Preferences>


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
        checkUserRole()
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

        val adapter = PostListAdapter(data) {
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
            findNavController().navigate(R.id.action_navigation_post_list_to_createPostFragment)
        }
    }

    private fun checkUserRole() {
        lifecycleScope.launch {
            readUserRole().collect {
                if (it == UserRole.ADMIN.name) {
                    binding.createPostFab.visibility = VISIBLE
                } else {
                    binding.createPostFab.visibility = GONE
                }
            }
        }
    }

    private fun readUserRole(): Flow<String> = prefDataStore.data
        .map { currentPreferences ->
            // Lets fetch the data from our DataStore by using the same key which we used earlier for storing the data.
            val name = currentPreferences[ROLE_KEY] ?: ""
            name
        }

}