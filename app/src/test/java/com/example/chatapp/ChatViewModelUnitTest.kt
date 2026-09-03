package com.example.chatapp

import android.net.Uri
import com.example.chatapp.data.models.Message
import com.example.chatapp.data.repository.MessageRepository
import com.example.chatapp.ui.chat.ChatUiEvent
import com.example.chatapp.ui.chat.ChatViewModel
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainDispatcherRule(
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
class ChatViewModelUnitTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ChatViewModel
    private lateinit var messageRepository: MessageRepository

    @Before
    fun setup() {
        messageRepository = mockk(relaxed = true)
        viewModel = ChatViewModel(messageRepository)
    }

    @Test
    fun `initialize calls observeMessages and updates state on success`() = runTest {
        val chatId = "chat123"
        val receiverId = "user456"
        val messages = listOf(
            Message(messageId = "1", text = "Hello", senderId = "me"),
            Message(messageId = "2", text = "Hi", senderId = receiverId)
        )

        every { messageRepository.observeMessages(chatId, any()) } answers {
            val callback = secondArg<(Result<List<Message>>) -> Unit>()
            callback(Result.success(messages))
            mockk()
        }

        viewModel.initialize(chatId, receiverId)

        assertEquals(messages, viewModel.uiState.value.messages)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `initialize updates state with error message on failure`() = runTest {
        val chatId = "chat123"
        val receiverId = "user456"
        val errorMsg = "Observation failed"

        every { messageRepository.observeMessages(chatId, any()) } answers {
            val callback = secondArg<(Result<List<Message>>) -> Unit>()
            callback(Result.failure(Exception(errorMsg)))
            mockk()
        }

        viewModel.initialize(chatId, receiverId)

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onEvent MessageTextChanged updates uiState`() {
        val text = "New message"
        viewModel.onEvent(ChatUiEvent.MessageTextChanged(text))
        assertEquals(text, viewModel.uiState.value.messageText)
    }

    @Test
    fun `sendTextMessage success clears messageText and sets isSending to false`() = runTest {
        val chatId = "chat123"
        val receiverId = "user456"
        val text = "Hello"

        viewModel.initialize(chatId, receiverId)
        viewModel.onEvent(ChatUiEvent.MessageTextChanged(text))

        coEvery { messageRepository.sendTextMessage(chatId, receiverId, text) } returns Result.success(Unit)

        viewModel.onEvent(ChatUiEvent.SendMessageClicked)

        assertEquals("", viewModel.uiState.value.messageText)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `sendTextMessage failure sets errorMessage and isSending to false`() = runTest {
        val chatId = "chat123"
        val receiverId = "user456"
        val text = "Hello"
        val errorMsg = "Send failed"

        viewModel.initialize(chatId, receiverId)
        viewModel.onEvent(ChatUiEvent.MessageTextChanged(text))

        coEvery { messageRepository.sendTextMessage(chatId, receiverId, text) } returns Result.failure(Exception(errorMsg))

        viewModel.onEvent(ChatUiEvent.SendMessageClicked)

        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `onEvent ImageSelected calls sendMediaMessage`() = runTest {
        val chatId = "chat123"
        val receiverId = "user456"
        val uri = mockk<Uri>()

        viewModel.initialize(chatId, receiverId)
        coEvery { messageRepository.sendMediaMessage(chatId, receiverId, uri, Message.MESSAGE_TYPE_IMAGE) } returns Result.success(Unit)

        viewModel.onEvent(ChatUiEvent.ImageSelected(uri))

        coVerify { messageRepository.sendMediaMessage(chatId, receiverId, uri, Message.MESSAGE_TYPE_IMAGE) }
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `onEvent BackClicked sends Navigate event`() = runTest {
        viewModel.onEvent(ChatUiEvent.BackClicked)
        val event = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.Navigate(AppRoutes.HomeRoute), event)
    }
}
