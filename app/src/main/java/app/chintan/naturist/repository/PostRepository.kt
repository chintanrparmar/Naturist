package app.chintan.naturist.repository

import android.graphics.Bitmap
import app.chintan.naturist.model.Post
import app.chintan.naturist.util.Constants.POST_COLLECTION
import app.chintan.naturist.util.State
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

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

    @ExperimentalCoroutinesApi
    fun uploadImage(bitmap: Bitmap): Flow<State<String>> = callbackFlow {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("mountains.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            trySendBlocking(State.success("Uploading Failed"))
                .onFailure { throwable ->
                    throwable?.message?.let {
                        State.error<String>(it)
                    }
                }
        }.addOnSuccessListener { taskSnapshot ->
            trySendBlocking(State.success("Successfully Uploaded"))
                .onFailure { throwable ->
                    throwable?.message?.let {
                        State.error<String>(it)
                    }
                }

        }

    }
}