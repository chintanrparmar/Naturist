package app.chintan.naturist.ui.blog

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chintan.naturist.model.Post
import app.chintan.naturist.repository.PostRepository
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

    private val _addPostLiveData = MutableLiveData<State<String>>()
    val addPostLiveData: LiveData<State<String>> = _addPostLiveData

    private val _selectedPostLiveData = MutableLiveData<State<Post>>()
    val selectedPostLiveData: LiveData<State<Post>> = _selectedPostLiveData

    private val _uploadImageLiveData = MutableLiveData<State<String>>()
    val uploadImageLiveData: LiveData<State<String>> = _uploadImageLiveData

    fun uploadImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _uploadImageLiveData.postValue(State.loading())
            postRepository.uploadImage(bitmap).collect {
                when (it) {
                    is State.Success -> {
                        _uploadImageLiveData.postValue(State.success(it.data))
                    }
                    is State.Error -> _uploadImageLiveData.postValue(State.error(it.message))
                    is State.Loading -> _uploadImageLiveData.postValue(State.loading())
                }
            }
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

    fun setSelectedPost(post: Post?) {
        if (post != null) {
            _selectedPostLiveData.postValue(State.success(post))
        } else {
            _selectedPostLiveData.postValue(State.error("Invalid Post Data"))
        }
    }
}