package com.example.chatapp

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chatapp.data.models.ChatListItem
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.ChatRepository
import com.example.chatapp.ui.home.HomeScreen
import com.example.chatapp.ui.home.HomeViewModel
import com.example.chatapp.utils.Constant.HOME_CHAT_LIST
import com.example.chatapp.utils.Constant.HOME_EMPTY_STATE
import com.example.chatapp.utils.Constant.HOME_LOGOUT_BUTTON
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenAndroidTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var navController: TestNavHostController

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        chatRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    private fun setupViewModelAndContent(chats: List<ChatListItem> = emptyList()) {
        every { chatRepository.observeActiveChats(any()) } answers {
            val callback = firstArg<(Result<List<ChatListItem>>) -> Unit>()
            callback(Result.success(chats))
            mockk(relaxed = true)
        }

        homeViewModel = HomeViewModel(
            chatRepository = chatRepository,
            authRepository = authRepository
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.HomeRoute
            ) {
                composable<AppRoutes.HomeRoute> {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onEvent = homeViewModel::onEvent,
                        navController = navController
                    )
                }
                composable<AppRoutes.LoginRoute> {
                    Text("Login Screen")
                }
                composable<AppRoutes.ChatRoute> {
                    Text("Chat Screen")
                }
            }
        }
    }

    @Test
    fun homeScreen_emptyState_displayedWhenNoChats() {
        setupViewModelAndContent(emptyList())

        composeTestRule.onNodeWithTag(HOME_EMPTY_STATE).assertIsDisplayed()
        composeTestRule.onNodeWithText("No active chats").assertIsDisplayed()
    }

    @Test
    fun homeScreen_chatList_displayedWhenChatsAvailable() {
        val mockUser = User(uid = "user1", name = "Jane Doe")
        val mockChat = ChatListItem(
            chatId = "chat1",
            user = mockUser,
            lastMessage = "Hello there!",
            lastMessageTime = System.currentTimeMillis()
        )

        setupViewModelAndContent(listOf(mockChat))

        composeTestRule.onNodeWithTag(HOME_CHAT_LIST).assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
    }

    @Test
    fun logoutButton_navigatesToLogin() {
        setupViewModelAndContent(emptyList())

        composeTestRule.onNodeWithTag(HOME_LOGOUT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.LoginRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.LoginRoute>()
        )
    }

    @Test
    fun clickingChat_navigatesToChatDetail() {
        val mockUser = User(uid = "user1", name = "Jane Doe")
        val mockChat = ChatListItem(
            chatId = "chat1",
            user = mockUser,
            lastMessage = "Hello there!",
            lastMessageTime = System.currentTimeMillis()
        )

        setupViewModelAndContent(listOf(mockChat))

        composeTestRule.onNodeWithText("Jane Doe").performClick()
        composeTestRule.waitForIdle()

        val currentRoute = navController.currentBackStackEntry?.toRoute<AppRoutes.ChatRoute>()
        assertEquals("chat1", currentRoute?.chatId)
        assertEquals("user1", currentRoute?.userId)
        assertEquals("Jane Doe", currentRoute?.userName)
    }
}
