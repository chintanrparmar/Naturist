package app.chintan.naturist.repository

import android.graphics.Bitmap
import app.chintan.naturist.model.Post
import app.chintan.naturist.util.Constants.POST_COLLECTION
import app.chintan.naturist.util.State
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

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

    fun addPost(post: Post) = flow<State<String>> {

        emit(State.loading())
        post.id = System.currentTimeMillis()
        val userRef = postCollectionRef.document(post.id.toString()).set(post).await()
        emit(State.success("Post Added"))

    }.catch {
        emit(State.error(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    fun likePost(postId: String, uid: String) = flow<State<DocumentReference>> {

        emit(State.loading())
        val washingtonRef = postCollectionRef.document(postId)

        washingtonRef.update("favourite", FieldValue.arrayUnion(uid))
        emit(State.success(washingtonRef))
    }.catch {
        emit(State.error(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    @ExperimentalCoroutinesApi
    suspend fun uploadImage(bitmap: Bitmap) =
        suspendCancellableCoroutine<State<String>> { continuation ->
            val storage = Firebase.storage
            val storageRef = storage.reference
            val ref = storageRef.child("images/mountains${System.currentTimeMillis()}.jpg")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = ref.putBytes(data)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        continuation.resumeWithException(it)
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    continuation.resume(State.success(downloadUri.toString()), null)
                } else {
                    continuation.resume(State.error("Uploading Failed"), null)
                }
            }
        }
}