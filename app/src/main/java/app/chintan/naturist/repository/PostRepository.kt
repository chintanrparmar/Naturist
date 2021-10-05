package app.chintan.naturist.repository

import app.chintan.naturist.model.Post
import app.chintan.naturist.util.Constants.POST_COLLECTION
import app.chintan.naturist.util.State
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val postCollectionRef = FirebaseFirestore.getInstance().collection(POST_COLLECTION)


    @ExperimentalCoroutinesApi
    fun getPosts(): Flow<State<List<Post>>> = callbackFlow {


        // Registers callback to firestore, which will be called on new events
        val subscription = postCollectionRef?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                return@addSnapshotListener
            }
            // Sends events to the flow! Consumers will get the new events

            trySendBlocking(State.success(snapshot.toObjects(Post::class.java)))
                .onFailure { throwable ->
                    throwable?.message?.let {
                        State.error<String>(it)
                    }
                }

        }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
    }

    fun addPost(post: Post) = flow<State<DocumentReference>> {

        emit(State.loading())

        val userRef = postCollectionRef.add(post).await()

        emit(State.success(userRef))

    }.catch {
        emit(State.error(it.message.toString()))
    }.flowOn(Dispatchers.IO)
}