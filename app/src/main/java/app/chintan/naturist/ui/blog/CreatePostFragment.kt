package app.chintan.naturist.ui.blog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.chintan.naturist.databinding.FragmentPostCreateBinding
import app.chintan.naturist.model.Post
import app.chintan.naturist.util.State
import coil.load
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels()
    private var _binding: FragmentPostCreateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPostCreateBinding.inflate(inflater, container, false)
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
    }

    private fun setUpObserver() {
        postViewModel.selectedImageBitmap.observe(viewLifecycleOwner, {
            when (it) {
                is State.Success -> binding.thumbnailIv.load(it.data)
                is State.Error -> Log.e(TAG, it.message)
            }

        })
        postViewModel.uploadImageLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is State.Success -> createPost(it.data)
                is State.Error -> Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                    .show()
                is State.Loading -> binding.progressBar.visibility = View.VISIBLE
            }

        })
        postViewModel.addPostLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is State.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().popBackStack()
                    Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                }
                is State.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
                is State.Loading -> binding.progressBar.visibility = View.VISIBLE
            }

        })
    }

    private fun createPost(url: String) {
        postViewModel.addPost(Post(imageUrl = url,
            title = binding.titleTil.editText?.text.toString(),
            description = binding.descTil.editText?.text.toString()))
    }


    private fun setUpOnClick() {

        binding.thumbnailIv.setOnClickListener { openCamera() }
        binding.submitBt.setOnClickListener { validateDataAndSubmit() }
    }

    private fun validateDataAndSubmit() {
        if (postViewModel.selectedImageBitmap.value == null || postViewModel.selectedImageBitmap.value is State.Error || postViewModel.getImageBitmap() == null) {
            Toast.makeText(requireContext(), "Select Valid Image", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.titleTil.editText?.text.toString().length < 2) {
            binding.titleTil.error = "Enter Valid Title"
            return
        }
        if (binding.descTil.editText?.text.toString().length < 10) {
            binding.descTil.error = "Enter Valid Description"
            return
        }

        postViewModel.getImageBitmap()?.let {
            postViewModel.uploadImage(it)
        }
    }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            postViewModel.setImageBitmap(it)
        }

    private fun openCamera() {
        takePhoto.launch()
    }


    companion object {
        const val TAG = "CreatePostFragment"
    }
}