package com.example.chatapp

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.login.LoginScreen
import com.example.chatapp.ui.login.LoginScreenViewModel
import com.example.chatapp.utils.Constant.EMAIL_TEXT_FIELD
import com.example.chatapp.utils.Constant.PASSWORD_TEXT_FIELD
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginScreenViewModel: LoginScreenViewModel
    private lateinit var navController: TestNavHostController

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        loginScreenViewModel = LoginScreenViewModel(authRepository = authRepository)

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.LoginRoute
            ) {
                composable<AppRoutes.LoginRoute> {
                    LoginScreen(loginScreenViewModel, navController)
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home")
                }
            }
        }
    }



    @Test
    fun login_emailField_enter() {
        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput("A@1.com")
        composeTestRule.waitForIdle()

        assertEquals("A@1.com", loginScreenViewModel.uiState.value.email)
    }

    @Test
    fun login_password_enter() {
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput("123456")
        composeTestRule.waitForIdle()

        assertEquals("123456", loginScreenViewModel.uiState.value.password)
    }


    @Test
    fun loginButton_disablesInputs_whileLoading() {
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(10_000) // stays "loading" for the duration of this test
            Result.success(mockk())
        }

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput("abc@gmail.com")
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput("123456")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).assertIsNotEnabled()
    }

    // ---- Success path ----

    @Test
    fun login_email_password_enter_success_navigate_to_home() {
        val email = "abc@gmail.com"
        val password = "123456"

        coEvery { authRepository.login(email, password) } returns Result.success(mockk())

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput(email)
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput(password)
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.HomeRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.HomeRoute>()
        )
    }

}