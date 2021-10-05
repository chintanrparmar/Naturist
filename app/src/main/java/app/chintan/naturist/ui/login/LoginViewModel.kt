package app.chintan.naturist.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chintan.naturist.model.UserProfile
import app.chintan.naturist.model.UserRole
import app.chintan.naturist.repository.UserRepository
import app.chintan.naturist.util.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {


    private val auth: FirebaseAuth = Firebase.auth

    private val _firebaseAuthLiveData = MutableLiveData<State<String>>()
    val firebaseAuthLiveData: LiveData<State<String>> = _firebaseAuthLiveData

    private val _updateUserRoleLiveData = MutableLiveData<State<String>>()
    val updateUserRoleLiveData: LiveData<State<String>> = _updateUserRoleLiveData


    fun firebaseAuthWithGoogle(idToken: String) {
        _firebaseAuthLiveData.postValue(State.loading())
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    _firebaseAuthLiveData.postValue(State.success("Account Verified"))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    _firebaseAuthLiveData.postValue(State.error(task.exception?.message.toString()))
                }
            }
    }

    fun updateUserRole(userRole: UserRole) {
        _updateUserRoleLiveData.postValue(State.loading())
        getCurrentUser().let {
            val userProfile = UserProfile()
            userProfile.uid = it?.uid
            userProfile.role = userRole
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.addUser(userProfile).collect { addUserState ->
                    when (addUserState) {
                        is State.Success -> {
                            if (addUserState.data.isEmpty()) {
                                _updateUserRoleLiveData.postValue(State.error("Something went wrong"))
                            } else {
                                _updateUserRoleLiveData.postValue(State.success(addUserState.data))
                            }
                        }
                        is State.Error -> _updateUserRoleLiveData.postValue(State.error(addUserState.message))
                        is State.Loading -> _updateUserRoleLiveData.postValue(State.loading())
                    }
                }

            }
        }
    }


    fun getCurrentUser() = auth.currentUser

    companion object {
        private const val TAG = "LoginViewModel"
    }
}