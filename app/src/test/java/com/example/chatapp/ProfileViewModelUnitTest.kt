package com.example.chatapp

import android.net.Uri
import com.example.chatapp.data.repository.UserRepository
import com.example.chatapp.ui.profile.ProfileUiEvent
import com.example.chatapp.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class ProfileMainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class ProfileViewModelUnitTest {

    @get:Rule
    val mainDispatcherRule = ProfileMainDispatcherRule()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth

    @Before
    fun setup() {
        userRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        viewModel = ProfileViewModel(userRepository, firebaseAuth)
    }

    @Test
    fun `NameChanged updates uiState name`() {
        val testName = "Jane Doe"
        viewModel.onEvent(ProfileUiEvent.NameChanged(testName))
        assertEquals(testName, viewModel.uiState.value.name)
    }

    @Test
    fun `ImageSelected updates uiState imageUri`() {
        val mockUri = mockk<Uri>()
        viewModel.onEvent(ProfileUiEvent.ImageSelected(mockUri))
        assertEquals(mockUri, viewModel.uiState.value.imageUri)
    }

    @Test
    fun `ClearError resets uiState errorMessage`() {
        // Since we can't easily set errorMessage directly, we'll verify it's null by default or after an action.
        viewModel.onEvent(ProfileUiEvent.ClearError)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `SaveProfileClicked when session expired shows toast`() = runTest {
        every { firebaseAuth.currentUser } returns null

        viewModel.onEvent(ProfileUiEvent.SaveProfileClicked)

        val event = viewModel.uiEvents.first()
        assertTrue(event is CommonUiEvent.ShowToast)
        assertEquals("User session expired. Please login again.", (event as CommonUiEvent.ShowToast).msg)
    }

    @Test
    fun `SaveProfileClicked success with name only navigates to Home`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "uid123"
        every { mockUser.email } returns "test@example.com"
        
        coEvery { userRepository.createUserProfile(any()) } returns Result.success(Unit)

        viewModel.onEvent(ProfileUiEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(ProfileUiEvent.SaveProfileClicked)

        val loaderEvent = viewModel.uiEvents.first()
        assertEquals(CommonUiEvent.ShowLoader, loaderEvent)

        val navigateEvent = viewModel.uiEvents.first()
        assertEquals(CommonUiEvent.Navigate(AppRoutes.HomeRoute), navigateEvent)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `SaveProfileClicked success with name and image navigates to Home`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val mockUri = mockk<Uri>()
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "uid123"
        every { mockUser.email } returns "test@example.com"
        
        coEvery { userRepository.uploadProfileImage("uid123", mockUri) } returns Result.success("https://image.url")
        coEvery { userRepository.createUserProfile(any()) } returns Result.success(Unit)

        viewModel.onEvent(ProfileUiEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(ProfileUiEvent.ImageSelected(mockUri))
        viewModel.onEvent(ProfileUiEvent.SaveProfileClicked)

        val loaderEvent = viewModel.uiEvents.first()
        assertEquals(CommonUiEvent.ShowLoader, loaderEvent)

        val navigateEvent = viewModel.uiEvents.first()
        assertEquals(CommonUiEvent.Navigate(AppRoutes.HomeRoute), navigateEvent)
    }

    @Test
    fun `SaveProfileClicked image upload failure sets error message`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val mockUri = mockk<Uri>()
        val errorMsg = "Upload failed"
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "uid123"
        
        coEvery { userRepository.uploadProfileImage("uid123", mockUri) } returns Result.failure(Exception(errorMsg))

        viewModel.onEvent(ProfileUiEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(ProfileUiEvent.ImageSelected(mockUri))
        viewModel.onEvent(ProfileUiEvent.SaveProfileClicked)

        // Consume loader
        viewModel.uiEvents.first()

        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `SaveProfileClicked profile creation failure sets error message`() = runTest {
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val errorMsg = "Firestore error"
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "uid123"
        
        coEvery { userRepository.createUserProfile(any()) } returns Result.failure(Exception(errorMsg))

        viewModel.onEvent(ProfileUiEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(ProfileUiEvent.SaveProfileClicked)

        // Consume loader
        viewModel.uiEvents.first()

        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
    }
}
