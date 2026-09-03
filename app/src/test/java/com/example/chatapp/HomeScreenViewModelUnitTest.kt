package com.example.chatapp

import com.example.chatapp.data.models.ChatListItem
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.ChatRepository
import com.example.chatapp.ui.home.HomeScreenEvent
import com.example.chatapp.ui.home.HomeViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class HomeMainDispatcherRule(
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
class HomeScreenViewModelUnitTest {

    @get:Rule
    val mainDispatcherRule = HomeMainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var chatRepository: ChatRepository
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        chatRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    @Test
    fun `init calls observeActiveChats and updates uiState on success`() = runTest {
        val mockChats = listOf(
            ChatListItem(chatId = "1", user = User(name = "User 1")),
            ChatListItem(chatId = "2", user = User(name = "User 2"))
        )

        every { chatRepository.observeActiveChats(any()) } answers {
            val callback = firstArg<(Result<List<ChatListItem>>) -> Unit>()
            callback(Result.success(mockChats))
            mockk(relaxed = true)
        }

        viewModel = HomeViewModel(chatRepository, authRepository)

        assertEquals(mockChats, viewModel.uiState.value.chats)
    }

    @Test
    fun `init calls observeActiveChats and sends ShowError event on failure`() = runTest {
        val errorMsg = "Failed to load chats"

        every { chatRepository.observeActiveChats(any()) } answers {
            val callback = firstArg<(Result<List<ChatListItem>>) -> Unit>()
            callback(Result.failure(Exception(errorMsg)))
            mockk(relaxed = true)
        }

        viewModel = HomeViewModel(chatRepository, authRepository)

        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.ShowError)
        assertEquals(errorMsg, (event as CommonUiEvent.ShowError).value)
    }

    @Test
    fun `onEvent NavigateToChat sends ShowLoader and Navigate events`() = runTest {
        // Initialize with success to avoid failure event interference if needed
        every { chatRepository.observeActiveChats(any()) } returns mockk(relaxed = true)
        viewModel = HomeViewModel(chatRepository, authRepository)

        val event = HomeScreenEvent.NavigateToChat(
            chatId = "chat1",
            userId = "user1",
            userName = "Jane",
            profileImageUrl = "url"
        )

        viewModel.onEvent(event)

        val loaderEvent = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.ShowLoader, loaderEvent)

        val navigationEvent = viewModel.uiEvent.first()
        assertEquals(
            CommonUiEvent.Navigate(
                AppRoutes.ChatRoute(
                    chatId = "chat1",
                    userId = "user1",
                    userName = "Jane",
                    profileImageUrl = "url"
                )
            ),
            navigationEvent
        )
    }

    @Test
    fun `onEvent LogoutClicked calls authRepository logout and navigates to Login`() = runTest {
        every { chatRepository.observeActiveChats(any()) } returns mockk(relaxed = true)
        viewModel = HomeViewModel(chatRepository, authRepository)

        viewModel.onEvent(HomeScreenEvent.LogoutClicked)

        val loaderEvent = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.ShowLoader, loaderEvent)

        verify { authRepository.logout() }

        val navigationEvent = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.Navigate(AppRoutes.LoginRoute), navigationEvent)
    }
}
