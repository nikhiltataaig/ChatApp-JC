package com.example.chatapp

import androidx.compose.material3.Text
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
import com.example.chatapp.data.repository.UserRepository
import com.example.chatapp.ui.profile.ProfileSetupScreen
import com.example.chatapp.ui.profile.ProfileViewModel
import com.example.chatapp.utils.Constant.PROFILE_CONTINUE_BUTTON
import com.example.chatapp.utils.Constant.PROFILE_NAME_FIELD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class SetupProfileScreenAndroidTest {

    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var navController: TestNavHostController

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        userRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_uid"
        every { mockUser.email } returns "test@example.com"

        profileViewModel = ProfileViewModel(
            userRepository = userRepository,
            firebaseAuth = firebaseAuth
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.SetupProfileRoute
            ) {
                composable<AppRoutes.SetupProfileRoute> {
                    ProfileSetupScreen(
                        navController = navController,
                        profileViewModel = profileViewModel
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home Screen")
                }
            }
        }
    }

    @Test
    fun profileName_inputWiring() {
        val testName = "John Doe"
        composeTestRule.onNodeWithTag(PROFILE_NAME_FIELD).performTextInput(testName)
        composeTestRule.waitForIdle()

        assertEquals(testName, profileViewModel.uiState.value.name)
    }

    @Test
    fun continueButton_disablesInputs_whileLoading() {
        coEvery { userRepository.createUserProfile(any()) } coAnswers {
            delay(10.seconds)
            Result.success(Unit)
        }

        composeTestRule.onNodeWithTag(PROFILE_NAME_FIELD).performTextInput("John Doe")
        composeTestRule.onNodeWithTag(PROFILE_CONTINUE_BUTTON).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PROFILE_NAME_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(PROFILE_CONTINUE_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun saveProfile_success_navigatesToHome() {
        val testName = "John Doe"
        coEvery { userRepository.createUserProfile(any()) } returns Result.success(Unit)

        composeTestRule.onNodeWithTag(PROFILE_NAME_FIELD).performTextInput(testName)
        composeTestRule.onNodeWithTag(PROFILE_CONTINUE_BUTTON).performClick()

        composeTestRule.waitForIdle()

        assertEquals(
            AppRoutes.HomeRoute,
            navController.currentBackStackEntry?.toRoute<AppRoutes.HomeRoute>()
        )
    }

    @Test
    fun saveProfile_failure_showsError() {
        val errorMsg = "Database error"
        coEvery { userRepository.createUserProfile(any()) } returns Result.failure(Exception(errorMsg))

        composeTestRule.onNodeWithTag(PROFILE_NAME_FIELD).performTextInput("John Doe")
        composeTestRule.onNodeWithTag(PROFILE_CONTINUE_BUTTON).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
    }
}
