package app.chintan.naturist.repository

import app.chintan.naturist.model.UserProfile
import app.chintan.naturist.util.Constants.POST_COLLECTION
import app.chintan.naturist.util.Constants.USER_COLLECTION
import app.chintan.naturist.util.State
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val userCollectionRef = FirebaseFirestore.getInstance().collection(USER_COLLECTION)

    fun addUser(user: UserProfile) = flow<State<String>> {

        emit(State.loading())

        val userRef = userCollectionRef.document(user.uid.toString()).set(user).await()

        emit(State.success("User Updated"))

    }.catch {
        emit(State.error(it.message.toString()))
    }.flowOn(Dispatchers.IO)


}