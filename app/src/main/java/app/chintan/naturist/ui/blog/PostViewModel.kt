package app.chintan.naturist.ui.blog

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chintan.naturist.model.Post
import app.chintan.naturist.repository.PostRepository
import app.chintan.naturist.util.FirebaseUserManager
import app.chintan.naturist.util.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val postRepository: PostRepository) : ViewModel() {

    private val _postList = MutableLiveData<State<List<Post>>>()
    val postList: LiveData<State<List<Post>>> = _postList

    private val _favouritePostList = MutableLiveData<State<List<Post>>>()
    val favouritePostList: LiveData<State<List<Post>>> = _favouritePostList

    private val _addPostLiveData = MutableLiveData<State<String>>()
    val addPostLiveData: LiveData<State<String>> = _addPostLiveData

    private val _likePostLiveData = MutableLiveData<State<String>>()
    val likePostLiveData: LiveData<State<String>> = _likePostLiveData

    private val _selectedPostLiveData = MutableLiveData<State<Post>>()
    val selectedPostLiveData: LiveData<State<Post>> = _selectedPostLiveData

    private val _uploadImageLiveData = MutableLiveData<State<String>>()
    val uploadImageLiveData: LiveData<State<String>> = _uploadImageLiveData

    private val _selectedImageBitmap = MutableLiveData<State<Bitmap>>()
    val selectedImageBitmap: LiveData<State<Bitmap>> = _selectedImageBitmap

    fun uploadImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _uploadImageLiveData.postValue(State.loading())
            val uploadImageState = postRepository.uploadImage(bitmap)
            _uploadImageLiveData.postValue(uploadImageState)
        }
    }

    fun addPost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addPost(post).collect {
                when (it) {
                    is State.Success -> {
                        if (it.data != null) {
                            _addPostLiveData.postValue(State.success("Added Successfully"))
                        } else {
                            _addPostLiveData.postValue(State.success("Something went wrong"))
                        }
                    }
                    is State.Error -> _addPostLiveData.postValue(State.error(it.message))
                    is State.Loading -> _addPostLiveData.postValue(State.loading())
                }
            }
        }
    }

    fun likePost(postId: String, uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.likePost(postId, uid).collect {
                when (it) {
                    is State.Success -> {
                        if (it.data != null) {
                            _likePostLiveData.postValue(State.success("Added Successfully"))
                        } else {
                            _likePostLiveData.postValue(State.success("Something went wrong"))
                        }
                    }
                    is State.Error -> _likePostLiveData.postValue(State.error(it.message))
                    is State.Loading -> _likePostLiveData.postValue(State.loading())
                }
            }
        }
    }

    fun getPosts() {

        viewModelScope.launch(Dispatchers.IO) {
            postRepository.getPosts().collect {
                when (it) {
                    is State.Success -> {
                        if (it.data.isNullOrEmpty()) {
                            _postList.postValue(State.error("No Data Found"))
                        } else {
                            _postList.postValue(State.success(it.data))
                        }
                    }
                    is State.Error -> _postList.postValue(State.error(it.message))
                    is State.Loading -> _postList.postValue(State.loading())
                }
            }

        }

    }

    fun getFavouritePosts() {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.getPosts().collect {
                when (it) {
                    is State.Success -> {
                        if (it.data.isNullOrEmpty()) {
                            _favouritePostList.postValue(State.error("No Data Found"))
                        } else {
                            val filterList=  it.data.filter { post -> post.favourite?.contains(FirebaseUserManager.getUserId()) == true }

                            _favouritePostList.postValue(State.success(filterList))
                        }
                    }
                    is State.Error -> _favouritePostList.postValue(State.error(it.message))
                    is State.Loading -> _favouritePostList.postValue(State.loading())
                }
            }

        }
    }

    fun setSelectedPost(post: Post?) {
        if (post != null) {
            _selectedPostLiveData.postValue(State.success(post))
        } else {
            _selectedPostLiveData.postValue(State.error("Invalid Post Data"))
        }
    }

    fun setImageBitmap(bitmap: Bitmap?) {

        if (bitmap == null) {
            _selectedImageBitmap.postValue(State.error("Invalid Image"))
            return
        }
        _selectedImageBitmap.postValue(State.success(bitmap))
    }

    fun getImageBitmap(): Bitmap? = when (selectedImageBitmap.value) {
        is State.Success -> (selectedImageBitmap.value as State.Success<Bitmap>).data
        else -> null
    }
}