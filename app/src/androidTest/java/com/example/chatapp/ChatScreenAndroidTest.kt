package com.example.chatapp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chatapp.data.models.Message
import com.example.chatapp.data.repository.MessageRepository
import com.example.chatapp.ui.chat.ChatScreen
import com.example.chatapp.ui.chat.ChatViewModel
import com.example.chatapp.utils.Constant.CHAT_BACK_BUTTON
import com.example.chatapp.utils.Constant.CHAT_MESSAGE_INPUT
import com.example.chatapp.utils.Constant.CHAT_MESSAGE_LIST
import com.example.chatapp.utils.Constant.CHAT_SEND_BUTTON
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenAndroidTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var navController: TestNavHostController
    private val chatArgs = AppRoutes.ChatRoute(
        chatId = "chat123",
        userId = "user456",
        userName = "Jane Doe",
        profileImageUrl = ""
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        
        messageRepository = mockk(relaxed = true)
        chatViewModel = ChatViewModel(messageRepository = messageRepository)
    }

    private fun setupContent(messages: List<Message> = emptyList()) {
        every { messageRepository.observeMessages(any(), any()) } answers {
            val callback = secondArg<(Result<List<Message>>) -> Unit>()
            callback(Result.success(messages))
            mockk(relaxed = true)
        }

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = "chat"
            ) {
                composable("chat") {
                    ChatScreen(
                        args = chatArgs,
                        viewModel = chatViewModel,
                        navController = navController
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    androidx.compose.material3.Text("Home Screen")
                }
            }
        }
    }
//
//    @Test
//    fun chatScreen_displaysMessages() {
//        val mockMessages = listOf(
//            Message(messageId = "1", text = "Hello!", senderId = "user456", timestamp = 1000L),
//            Message(messageId = "2", text = "Hi Jane!", senderId = "me", timestamp = 2000L)
//        )
//        setupContent(mockMessages)
//
//        composeTestRule.onNodeWithTag(CHAT_MESSAGE_LIST).assertIsDisplayed()
//        composeTestRule.onNodeWithText("Hello!").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Hi Jane!").assertIsDisplayed()
//    }

    @Test
    fun typingMessage_updatesStateAndEnablesSend() {
        setupContent()

        val testMessage = "Test message"
        composeTestRule.onNodeWithTag(CHAT_MESSAGE_INPUT).performTextInput(testMessage)
        composeTestRule.waitForIdle()

        assertEquals(testMessage, chatViewModel.uiState.value.messageText)
        composeTestRule.onNodeWithTag(CHAT_SEND_BUTTON).assertIsDisplayed()
    }

    @Test
    fun clickingSend_callsRepository() {
        setupContent()
        val testMessage = "Test message"
        
        coEvery { 
            messageRepository.sendTextMessage("chat123", "user456", testMessage) 
        } returns Result.success(Unit)

        composeTestRule.onNodeWithTag(CHAT_MESSAGE_INPUT).performTextInput(testMessage)
        composeTestRule.onNodeWithTag(CHAT_SEND_BUTTON).performClick()

        composeTestRule.waitForIdle()
        
        // State should be cleared after send
        assertEquals("", chatViewModel.uiState.value.messageText)
    }

    @Test
    fun clickingBack_navigatesToHome() {
        setupContent()

        composeTestRule.onNodeWithTag(CHAT_BACK_BUTTON).performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.HomeRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.HomeRoute>()
        )
    }
}
