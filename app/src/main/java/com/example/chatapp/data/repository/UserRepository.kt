package com.example.chatapp.data.repository


import android.net.Uri
import com.example.chatapp.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseAuth : FirebaseAuth
) {

    suspend fun uploadProfileImage(
        uid: String,
        imageUri: Uri
    ): Result<String> {

        return try {

            val imageReference = storage
                .reference
                .child("profile_images")
                .child(uid)
                .child("profile.jpg")

            imageReference.putFile(imageUri).await()

            val downloadUrl =
                imageReference.downloadUrl.await()

            Result.success(downloadUrl.toString())

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun createUserProfile(
        user: User
    ): Result<Unit> {

        return try {

            firestore
                .collection("users")
                .document(user.uid)
                .set(user)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<User>> {

        return try {


            val currentUserId =
                firebaseAuth.currentUser?.uid
                    ?: throw Exception(
                        "User session expired."
                    )

            val snapshot =
                firestore
                    .collection("users")
                    .get()
                    .await()

            val users =
                snapshot.documents
                    .mapNotNull {
                        it.toObject(User::class.java)
                    }
                    .filter {
                        it.uid != currentUserId
                    }

            Result.success(users)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    suspend fun getUserProfile(
        uid: String
    ): Result<User> {

        return try {

            val snapshot = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(
                    Exception("User profile not found.")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}