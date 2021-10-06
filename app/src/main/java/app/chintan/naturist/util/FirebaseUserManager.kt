package app.chintan.naturist.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object FirebaseUserManager {

    private var auth: FirebaseAuth = Firebase.auth
    fun getAuth() = auth
    fun getCurrentUser() = auth.currentUser
    fun getUserId() = auth.uid

}