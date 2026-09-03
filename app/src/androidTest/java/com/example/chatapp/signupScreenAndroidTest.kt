package com.example.chatapp

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.auth.AuthViewModel
import com.example.chatapp.ui.auth.SignupScreen
import com.example.chatapp.utils.Constant.CONFIRM_PASSWORD_TEXT_FIELD
import com.example.chatapp.utils.Constant.EMAIL_TEXT_FIELD
import com.example.chatapp.utils.Constant.PASSWORD_TEXT_FIELD
import com.example.chatapp.utils.Constant.SIGNUP_BUTTON
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class SignupScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: TestNavHostController

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        authViewModel = AuthViewModel(authRepository = authRepository)

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.SignupRoute
            ) {
                composable<AppRoutes.SignupRoute> {
                    SignupScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        modifier = Modifier
                    )
                }
                composable<AppRoutes.SetupProfileRoute> {
                    Text("Setup Profile")
                }
                composable<AppRoutes.LoginRoute> {
                    Text("Login")
                }
            }
        }
    }

    @Test
    fun signup_emailField_enter() {
        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput("test@example.com")
        composeTestRule.waitForIdle()

        assertEquals("test@example.com", authViewModel.uiState.value.email)
    }

    @Test
    fun signup_passwordFields_enter() {
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.waitForIdle()

        assertEquals("password123", authViewModel.uiState.value.password)
        assertEquals("password123", authViewModel.uiState.value.confirmPassword)
    }

    @Test
    fun signupButton_disablesInputs_whileLoading() {
        coEvery { authRepository.signup(any(), any()) } coAnswers {
            delay(10.seconds)
            Result.success(mockk())
        }

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput("test@example.com")
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(SIGNUP_BUTTON).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(CONFIRM_PASSWORD_TEXT_FIELD).assertIsNotEnabled()
    }

    @Test
    fun signup_success_navigatesToSetupProfile() {
        val email = "test@example.com"
        val password = "password123"

        coEvery { authRepository.signup(email, password) } returns Result.success(mockk())

        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput(email)
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput(password)
        composeTestRule.onNodeWithTag(CONFIRM_PASSWORD_TEXT_FIELD).performTextInput(password)
        composeTestRule.onNodeWithTag(SIGNUP_BUTTON).performClick()

        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.SetupProfileRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.SetupProfileRoute>()
        )
    }

    @Test
    fun signup_passwordsDoNotMatch_showsError() {
        composeTestRule.onNodeWithTag(EMAIL_TEXT_FIELD).performTextInput("test@example.com")
        composeTestRule.onNodeWithTag(PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("wrongpassword")
        composeTestRule.onNodeWithTag(SIGNUP_BUTTON).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Passwords do not match.").assertIsDisplayed()
    }

    @Test
    fun loginClicked_navigatesToLogin() {
        composeTestRule.onNodeWithText("Already have an account? Login").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.LoginRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.LoginRoute>()
        )
    }
}
