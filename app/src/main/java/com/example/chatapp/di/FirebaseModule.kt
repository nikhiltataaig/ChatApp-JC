package com.example.chatapp.di


import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp{
        val options = FirebaseOptions.Builder()
            .setProjectId("chatapp-x-265a9")
            .setApplicationId("1:900598673926:android:bc41f3c5d13958d8418987")
            .setApiKey("AIzaSyAOUJWoONwQMS5n9JrQ92xGd49C7nh-mgc")
            .setStorageBucket("chatapp-x-265a9.firebasestorage.app")
            .build()


        return FirebaseApp.initializeApp(context, options)
    }


    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        val instance = FirebaseAuth.getInstance(firebaseApp)
        Log.d("FirebaseModule","provideFirebaseAuth called")
        return instance
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}


